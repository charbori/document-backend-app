pipeline {
    agent any

    environment {
        DOCUMENT_APP_DOMAIN_URL          = credentials('DOCUMENT_APP_DOMAIN_URL')
        DOCUMENT_APP_DOMAIN_FRONT_URL    = credentials('DOCUMENT_APP_DOMAIN_FRONT_URL')
        DOCUMENT_APP_DATASOURCE_USERNAME = credentials('DOCUMENT_APP_DATASOURCE_USERNAME')
        DOCUMENT_APP_DATASOURCE_PASSWORD = credentials('DOCUMENT_APP_DATASOURCE_PASSWORD')
        DOCUMENT_APP_AES_SECRET_KEY      = credentials('DOCUMENT_APP_AES_SECRET_KEY')
        DOCUMENT_APP_JWT_SECRET          = credentials('DOCUMENT_APP_JWT_SECRET')
        //DOCUMENT_APP_DEPLOY_SSH_KEY      = credentials('DOCUMENT_APP_DEPLOY_SSH_KEY')

        // 도커 배포
        DOCKER_IMAGE_NAME = 'web-differ'
        DOCKER_IMAGE_TAG = "build-${env.BUILD_NUMBER}"
    }

    tools {
        jdk 'JDK_17'
    }

    stages {
        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/charbori/document-backend-app.git'
            }
        }

        stage('Validate') {
            steps {
                sh 'chmod +x ./gradlew'
                sh './gradlew test'
            }
        }

        stage('Build') {
            steps {
                script {
                    sh './gradlew clean build'
                    def jarFile = findFiles(glob: 'build/libs/web-differ*.jar')[0]
                    if (!jarFile) {
                        error "빌드된 JAR 파일을 찾을 수 없습니다. 빌드가 실패했거나 파일 이름이 다릅니다."
                    }
                    echo "Found JAR file: ${jarFile.path}"

                    // 3. Dockerfile이 찾기 쉬운 이름으로 Workspace 내에서 이름 변경
                    // cp를 외부로 하는 대신, Workspace 내에서 이름을 변경합니다.
                    // Dockerfile의 COPY 명령어와 경로/이름을 일치시킵니다.
                    // 예: Dockerfile이 'build/libs/web-differ.jar'를 복사한다고 가정
                    sh "mv ${jarFile.path} build/libs/web-differ.jar"
                    sh 'echo "Build Docker image: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}..." >> /home/ubuntu/video-manager-server/app/deploy-${env.BUILD_NUMBER}.log'
                    sh 'docker build -t ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}'
                }
            }
        }

        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                //input message: "정말로 프로덕션 서버(150.230.253.79)에 배포하시겠습니까?", ok: "배포 시작"
                /* //원격 서버 배포스크립트
                script {
                    sshagent(credentials: ['DOCUMENT_APP_DEPLOY_SSH_KEY']) {
                        def remoteUser = 'ubuntu'
                        def remoteHost = '150.230.253.79'
                        def remoteDir = '/home/ubuntu/app'
                        def jarFile = findFiles(glob: 'build/libs/web-differ*.jar')[0]
                        def appName = jarFile.name
                        def appLog = "${remoteDir}/app-${env.BUILD_NUMBER}.log"
                        def deployLog = "${remoteDir}/deploy-${env.BUILD_NUMBER}.log"

                        sh "scp -o StrictHostKeyChecking=no ${jarFile.path} ${remoteUser}@${remoteHost}:${remoteDir}/"


                        sh """
                            ssh -o StrictHostKeyChecking=no ${remoteUser}@${remoteHost} '
                                echo "=================================================================" >> ${deployLog}
                                echo "Starting new deployment..." >> ${deployLog}
                                echo "Deploy Time      : \$(date)" >> ${deployLog}
                                echo "Jenkins Build    : #${env.BUILD_NUMBER}" >> ${deployLog}
                                echo "Jenkins Build URL: ${env.BUILD_URL}" >> ${deployLog}
                                echo "Git Commit Hash  : ${env.GIT_COMMIT}" >> ${deployLog}
                                echo "Deployed JAR     : ${appName}" >> ${deployLog}
                                echo "=================================================================" >> ${deployLog}

                                # 기존에 실행 중인 애플리케이션 프로세스를 종료합니다.
                                PID=\$(pgrep -f ${appName})
                                if [ -n "\$PID" ]; then
                                    echo "Waiting for process to terminate..." >> ${deployLog}
                                    for i in {1..3}; do
                                        if ! pgrep -f ${appName} > /dev/null; then
                                            echo "Process terminated successfully." >> ${deployLog}
                                            break
                                        fi
                                        echo -n "."
                                        sleep 1
                                    done
                                fi

                                # 환경 변수를 주입하여 새 애플리케이션을 백그라운드로 실행합니다.
                                echo "Starting new process..." >> ${deployLog}

                                export DOCUMENT_APP_DOMAIN_URL="${env.DOCUMENT_APP_DOMAIN_URL}"
                                export DOCUMENT_APP_DOMAIN_FRONT_URL="${env.DOCUMENT_APP_DOMAIN_FRONT_URL}"
                                export DOCUMENT_APP_DATASOURCE_USERNAME="${env.DOCUMENT_APP_DATASOURCE_USERNAME}"
                                export DOCUMENT_APP_DATASOURCE_PASSWORD="${env.DOCUMENT_APP_DATASOURCE_PASSWORD}"
                                export DOCUMENT_APP_AES_SECRET_KEY="${env.DOCUMENT_APP_AES_SECRET_KEY}"
                                export DOCUMENT_APP_JWT_SECRET="${env.DOCUMENT_APP_JWT_SECRET}"
                                echo "${DOCUMENT_APP_AES_SECRET_KEY}" >> ${deployLog}

                                nohup java -jar ${remoteDir}/${appName} --spring.profiles.active=prod >> ${appLog} 2>&1 &
                                
                                echo "Deployment completed successfully." >> ${deployLog}
                            '
                        """
                    }
                }
                    */

                // DOCKER 스크립트
                // 로컬서버에서 앱 기동시 스크립트
                script {
                    def DEPLOY_LOG = "/home/ubuntu/video-manager-server/app/deploy-${env.BUILD_NUMBER}.log"

                    sh """
                        #!/bin/bash
                        set -e

                        # 1. 어느 포트에 배포할지 결정
                        # 8889 포트에서 실행 중인 컨테이너가 있는지 확인
                        if docker ps -q -f "publish=8889" | grep -q .; then
                            echo "Current app is running on port 8889. Deploying to port 8890." >> \${DEPLOY_LOG}
                            IDLE_PORT=8890
                            OLD_PORT=8889
                        else
                            echo "Current app is running on port 8890 (or none). Deploying to port 8889." >> \${DEPLOY_LOG}
                            IDLE_PORT=8889
                            OLD_PORT=8890
                        fi

                        # 2. 새 버전 컨테이너 생성
                        echo "Build app lit " >> \${DEPLOY_LOG}
                        docker images | grep web-differ >> \${DEPLOY_LOG}

                        echo "Starting new container on port \${IDLE_PORT}..." >> \${DEPLOY_LOG}
                        # 컨테이너 이름에 포트 번호를 넣어 식별 용이하게 함
                        docker run -d --name web-differ-\${IDLE_PORT} -p \${IDLE_PORT}:8080 \\
                            -e SPRING_PROFILES_ACTIVE=prod \\
                            -e DOCUMENT_APP_DOMAIN_URL="\${env.DOCUMENT_APP_DOMAIN_URL}" \\
                            -e DOCUMENT_APP_DOMAIN_FRONT_URL="\${env.DOCUMENT_APP_DOMAIN_FRONT_URL}" \\
                            -e DOCUMENT_APP_DATASOURCE_USERNAME="\${env.DOCUMENT_APP_DATASOURCE_USERNAME}" \\
                            -e DOCUMENT_APP_DATASOURCE_PASSWORD="\${env.DOCUMENT_APP_DATASOURCE_PASSWORD}" \\
                            -e DOCUMENT_APP_AES_SECRET_KEY="\${env.DOCUMENT_APP_AES_SECRET_KEY}" \\
                            \${DOCKER_IMAGE_NAME}:\${DOCKER_IMAGE_TAG}

                        # 3. 상태 확인 (Health Check)
                        echo "Waiting for health check on port \${IDLE_PORT}..." >> \${DEPLOY_LOG}
                        sleep 10

                        for i in {1..3}; do
                            RESPONSE_CODE=\$(curl -s -o /dev/null -w "%{http_code}" http://localhost:\${IDLE_PORT}/actuator/health )
                            if [ "\$RESPONSE_CODE" -ge 200 ] && [ "\$RESPONSE_CODE" -lt 400 ]; then
                                echo "Health check successful." >> \${DEPLOY_LOG}
                                break
                            fi
                            echo "Health check failed. Retrying..." >> \${DEPLOY_LOG}
                            sleep 5
                        done

                        if [ "\$i" -eq 3 ]; then
                            echo "Health check failed. Rolling back..." >> \${DEPLOY_LOG}
                            docker stop web-differ-\${IDLE_PORT} && docker rm web-differ-\${IDLE_PORT}
                            exit 1
                        fi

                        # 4. 구 버전 컨테이너 종료
                        OLD_CONTAINER_ID=\$(docker ps -q -f "publish=\${OLD_PORT}")
                        if [ -n "\$OLD_CONTAINER_ID" ]; then
                            echo "Stopping and removing old container on port \${OLD_PORT}..." >> \${DEPLOY_LOG}
                            docker stop \$OLD_CONTAINER_ID && docker rm \$OLD_CONTAINER_ID
                        else
                            echo "No old container found to stop." >> \${DEPLOY_LOG}
                        fi

                        echo "### Deployment to port \${IDLE_PORT} completed successfully ###" >> \${DEPLOY_LOG}
                    """
                }
            }
        }
    }

    post {
        always {
            archiveArtifacts artifacts: 'build/libs/*.jar', fingerprint: true
            cleanWs()
        }
        success {
            echo 'Pipeline successfully completed!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}