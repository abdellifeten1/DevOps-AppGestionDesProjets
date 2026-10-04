pipeline {
    agent any

    tools {
        jdk 'JAVA_HOME'
        maven 'M2_HOME'
    }

    environment {
        DOCKER_HUB_CREDS = credentials('dockerhub-credentials')
        SONAR_TOKEN      = credentials('sonarqube-token')
        BACKEND_IMAGE    = 'fetenabd/devops-backend'
        FRONTEND_IMAGE   = 'fetenabd/devops-frontend'
        IMAGE_TAG        = "${BUILD_NUMBER}"
    }

    stages {
        stage('1. Git Checkout') {
            steps {
                echo '=== Stage 1 : Checkout SCM ==='
                checkout scm
                sh 'git log --oneline -3'
            }
        }

        stage('2. Maven Compile') {
            steps {
                echo '=== Stage 2 : Maven compile ==='
                dir('backend') {
                    sh 'mvn clean compile -DskipTests'
                }
            }
        }

        stage('3. SonarQube Analysis') {
            steps {
                echo '=== Stage 3 : SonarQube ==='
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            mvn sonar:sonar \
                              -Dsonar.projectKey=DevOps-AppGestionDesProjets \
                              -Dsonar.projectName=DevOps-AppGestionDesProjets \
                              -Dsonar.sources=src/main/java \
                              -Dsonar.login=$SONAR_TOKEN
                        '''
                    }
                }
            }
        }

        stage('3b. Quality Gate') {
            steps {
                echo '=== Stage 3b : Quality Gate ==='
                timeout(time: 5, unit: 'MINUTES') {
                    waitForQualityGate abortPipeline: false
                }
            }
        }

        stage('4. Maven Test') {
            steps {
                echo '=== Stage 4 : Maven test ==='
                dir('backend') {
                    sh 'mvn test'
                }
            }
            post {
                always {
                    junit 'backend/target/surefire-reports/*.xml'
                }
            }
        }

        stage('5. Maven Package') {
            steps {
                echo '=== Stage 5 : Maven package ==='
                dir('backend') {
                    sh 'mvn package -DskipTests'
                }
                archiveArtifacts artifacts: 'backend/target/*.jar', fingerprint: true
            }
        }

        stage('6. Maven Install (deploy local)') {
            steps {
                echo '=== Stage 6 : Maven install ==='
                dir('backend') {
                    sh 'mvn install -DskipTests'
                }
            }
        }

        stage('7. Docker Build & Push') {
            steps {
                echo '=== Stage 7 : Docker build + push ==='
                sh "echo \$DOCKER_HUB_CREDS_PSW | docker login -u \$DOCKER_HUB_CREDS_USR --password-stdin"
                sh "docker build -t ${BACKEND_IMAGE}:${IMAGE_TAG} -t ${BACKEND_IMAGE}:latest ./backend"
                sh "docker build -t ${FRONTEND_IMAGE}:${IMAGE_TAG} -t ${FRONTEND_IMAGE}:latest ./frontend"
                sh "docker push ${BACKEND_IMAGE}:${IMAGE_TAG}"
                sh "docker push ${BACKEND_IMAGE}:latest"
                sh "docker push ${FRONTEND_IMAGE}:${IMAGE_TAG}"
                sh "docker push ${FRONTEND_IMAGE}:latest"
            }
        }

        stage('8. Docker Compose Up') {
            steps {
                echo '=== Stage 8 : Docker compose up ==='
                sh 'docker compose down || true'
                sh 'docker compose up -d --build'
                sh 'sleep 30'
                sh 'docker compose ps'
            }
        }
    }

    post {
        always {
            sh 'docker compose logs --tail 30 || true'
        }
        success {
            echo ' Pipeline terminé.'
        }
        failure {
            echo ' Pipeline échoué.'
        }
    }
}
