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
                    sh """
                        #!/bin/bash
                        set -e

                        echo "Build Docker image: ${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}..."
                        echo "Jenkins 파이프라인을 실행하는 사용자: \$(whoami)"
                        echo "사용자의 그룹 정보: \$(id -Gn)"
                        docker ps
                    """
                    def customImage = docker.build("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}")
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
                    def DEPLOY_LOG = "/var/log/deploy/deploy-${env.BUILD_NUMBER}.log"
                    def IDLE_PORT
                    def OLD_PORT

                    // 1. 어느 포트에 배포할지 결정
                    // 8889 포트에서 실행 중인 컨테이너가 있는지 확인
                    // docker.ps()는 실행 중인 컨테이너 ID 목록을 반환합니다.
                    def runningOn8889 = sh(script: 'docker ps -q -f "publish=8889"', returnStdout: true).trim()

                    if (runningOn8889) {
                        echo "현재 앱이 8889 포트에서 실행 중입니다. 8890 포트에 배포합니다."
                        IDLE_PORT = 8890
                        OLD_PORT = 8889
                    } else {
                        echo "현재 앱이 8890 포트(또는 없음)에서 실행 중입니다. 8889 포트에 배포합니다."
                        IDLE_PORT = 8889
                        OLD_PORT = 8890
                    }

                    // 로그 파일에 기록
                    sh "echo 'Deploying to IDLE_PORT: ${IDLE_PORT}' >> ${DEPLOY_LOG}"

                    // 2. 새 버전 컨테이너 생성
                    echo "새 컨테이너를 ${IDLE_PORT} 포트에서 시작합니다..."
                    // docker.image(...).run(...)을 사용하여 컨테이너 실행
                    def newContainer = docker.image("${DOCKER_IMAGE_NAME}:${DOCKER_IMAGE_TAG}").run(
                        "--name web-differ-${IDLE_PORT} -p ${IDLE_PORT}:8080 " +
                        "-e SPRING_PROFILES_ACTIVE=prod " +
                        "-e DOCUMENT_APP_DOMAIN_URL=${env.DOCUMENT_APP_DOMAIN_URL} " +
                        "-e DOCUMENT_APP_DOMAIN_FRONT_URL=${env.DOCUMENT_APP_DOMAIN_FRONT_URL} " +
                        "-e DOCUMENT_APP_DATASOURCE_USERNAME=${env.DOCUMENT_APP_DATASOURCE_USERNAME} " +
                        "-e DOCUMENT_APP_DATASOURCE_PASSWORD=${env.DOCUMENT_APP_DATASOURCE_PASSWORD} " +
                        "-e DOCUMENT_APP_AES_SECRET_KEY=${env.DOCUMENT_APP_AES_SECRET_KEY}"
                        "-e DOCUMENT_APP_JWT_SECRET=${env.DOCUMENT_APP_JWT_SECRET}"
                    )

                    // 3. 상태 확인 (Health Check)
                    echo "포트 ${IDLE_PORT}의 상태 확인을 시작합니다..."
                    sh "sleep 10" // 컨테이너가 시작될 시간을 줍니다.

                    def healthCheckOk = false
                    for (int i = 0; i < 3; i++) {
                        try {
                            def responseCode = sh(
                                script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:${IDLE_PORT}/actuator/health",
                                returnStdout: true
                             ).trim()

                            if (responseCode.toInteger() >= 200 && responseCode.toInteger() < 400) {
                                echo "상태 확인 성공."
                                healthCheckOk = true
                                break
                            }
                        } catch (Exception e) {
                            // curl 명령이 실패할 경우 (예: 네트워크 문제)
                            echo "상태 확인 중 오류 발생: ${e.message}"
                        }
                        echo "상태 확인 실패. 5초 후 재시도합니다..."
                        sh "sleep 5"
                    }

                    if (!healthCheckOk) {
                        echo "상태 확인에 최종 실패했습니다. 롤백을 시작합니다..."
                        sh "echo 'Health check failed. Rolling back...' >> ${DEPLOY_LOG}"
                        newContainer.stop()
                        // newContainer.remove()는 지원되지 않으므로 sh 명령 사용
                        sh "docker rm web-differ-${IDLE_PORT}"
                        error "배포 실패: 상태 확인 실패"
                    }

                    // 4. 구 버전 컨테이너 종료
                    def oldContainerId = sh(script: "docker ps -q -f 'publish=${OLD_PORT}'", returnStdout: true).trim()
                    if (oldContainerId) {
                        echo "${OLD_PORT} 포트의 이전 컨테이너를 중지하고 제거합니다..."
                        sh "docker stop ${oldContainerId} && docker rm ${oldContainerId}"
                    } else {
                        echo "중지할 이전 컨테이너가 없습니다."
                    }

                    echo "### ${IDLE_PORT} 포트로의 배포가 성공적으로 완료되었습니다. ###"
                    sh "echo 'Deployment to port ${IDLE_PORT} completed successfully' >> ${DEPLOY_LOG}"
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