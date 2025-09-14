pipeline {
    agent any

    environment {
        // --- 1. Jenkins Global Credentials 불러오기 ---
        // Jenkins > Manage Jenkins > Credentials 에서 등록한 ID와 일치해야 합니다.
        // 각 Credential의 종류는 'Secret text'로 가정합니다.
        DOCUMENT_APP_DOMAIN_URL          = credentials('DOCUMENT_APP_DOMAIN_URL')
        DOCUMENT_APP_DOMAIN_FRONT_URL    = credentials('DOCUMENT_APP_DOMAIN_FRONT_URL')
        DOCUMENT_APP_DATASOURCE_USERNAME = credentials('DOCUMENT_APP_DATASOURCE_USERNAME')
        DOCUMENT_APP_DATASOURCE_PASSWORD = credentials('DOCUMENT_APP_DATASOURCE_PASSWORD')
        DOCUMENT_APP_AES_SECRET_KEY      = credentials('DOCUMENT_APP_AES_SECRET_KEY')
        DOCUMENT_APP_JWT_SECRET          = credentials('DOCUMENT_APP_JWT_SECRET')
        //DOCUMENT_APP_DEPLOY_SSH_KEY      = credentials('DOCUMENT_APP_DEPLOY_SSH_KEY')

    }

    tools {
        // Jenkins > Global Tool Configuration 에 설정된 JDK 이름을 사용합니다.
        jdk 'JDK_17'
    }

    stages {
        stage('Checkout') {
            steps {
                // main 브랜치의 소스코드를 가져옵니다.
                git branch: 'main', url: 'https://github.com/charbori/document-backend-app.git'
            }
        }

        stage('Validate') {
            steps {
                // Gradle Wrapper 실행 권한 부여 및 테스트 실행
                sh 'chmod +x ./gradlew'
                sh './gradlew test'
            }
        }

        stage('Build') {
            steps {
                // 애플리케이션을 빌드하여 JAR 파일을 생성합니다.
                sh './gradlew clean build'
            }
        }

        // --- 2. 배포 스테이지 (main 브랜치일 경우에만 실행) ---
        stage('Deploy to Production') {
            when {
                branch 'main'
            }
            steps {
                // 배포 전 수동 승인 단계 (안전장치)
                //input message: "정말로 프로덕션 서버(150.230.253.79)에 배포하시겠습니까?", ok: "배포 시작"
                
                script {
                    // --- 3. SSH Agent를 사용하여 원격 서버에 접속 ---
                    // 'deploy-server-ssh-key'는 Jenkins에 등록한 SSH Credential의 ID 입니다.
                    sshagent(credentials: ['DOCUMENT_APP_DEPLOY_SSH_KEY']) {

                        // 변수 설정
                        def remoteUser = 'ubuntu' // 👈 배포 서버 접속 유저 이름으로 변경하세요.
                        def remoteHost = '150.230.253.79'
                        def remoteDir = '/home/ubuntu/app' // 👈 JAR 파일을 업로드할 서버 디렉토리
                        def jarFile = findFiles(glob: 'build/libs/web-differ*.jar')[0]
                        def appName = jarFile.name
                        def appLog = "${remoteDir}/app-${env.BUILD_NUMBER}.log"
                        def deployLog = "${remoteDir}/deploy-${env.BUILD_NUMBER}.log"

                        echo "Deploying ${appName} to ${remoteUser}@${remoteHost}"

                        // --- 4. scp를 이용해 JAR 파일 전송 ---
                        sh "scp -o StrictHostKeyChecking=no ${jarFile.path} ${remoteUser}@${remoteHost}:${remoteDir}/"

                        // --- 5. ssh를 이용해 원격 배포 스크립트 실행 ---
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
            }
        }
    }

    post {
        always {
            // 빌드 결과물(JAR)을 Jenkins에 보관하고 작업 공간을 정리합니다.
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