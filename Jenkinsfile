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

        stage('3. Maven Test') {
            steps {
                echo '=== Stage 3 : Maven test + JaCoCo ==='
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

        stage('4. SonarQube Analysis') {
            steps {
                echo '=== Stage 4 : SonarQube (avec coverage) ==='
                dir('backend') {
                    withSonarQubeEnv('SonarQube') {
                        sh '''
                            mvn sonar:sonar \
                              -Dsonar.projectKey=DevOps-AppGestionDesProjets \
                              -Dsonar.projectName=DevOps-AppGestionDesProjets \
                              -Dsonar.sources=src/main/java \
                              -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml \
                              -Dsonar.coverage.exclusions=**/entity/**,**/BackendApplication.java \
                              -Dsonar.login=$SONAR_TOKEN
                        '''
                    }
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

        stage('8. Kubernetes Deploy') {
            steps {
                echo '=== Stage 8 : Déploiement Kubernetes ==='

                // 1. Arrêter Docker Compose (évite conflits de ports)
                sh 'docker compose down || true'

                // 2. Nettoyer l'ancien déploiement K8s
                sh 'kubectl delete -f k8s/ --ignore-not-found=true || true'
                sh 'sleep 5'

                // 3. Namespace
                sh 'kubectl apply -f k8s/namespace.yaml'

                // 4. MySQL (PV + PVC + Deployment + Service ClusterIP)
                sh 'kubectl apply -f k8s/mysql.yaml'
                sh 'kubectl rollout status deployment/mysql -n devops --timeout=180s'

                // 5. Backend (ConfigMap + Secret + Deployment + Service NodePort)
                sh 'kubectl apply -f k8s/backend.yaml'
                sh 'kubectl rollout status deployment/backend -n devops --timeout=180s'

                // 6. Frontend (Deployment + Service NodePort)
                sh 'kubectl apply -f k8s/frontend.yaml'
                sh 'kubectl rollout status deployment/frontend -n devops --timeout=180s'

                // 7. Vérifications finales
                sh 'kubectl get pods -n devops'
                sh 'kubectl get svc -n devops'
            }
        }
    }

    post {
        always {
            sh 'echo "=== Logs Docker (si Docker Compose utilisé) ==="'
            sh 'docker compose logs --tail 20 || true'
            sh 'echo "=== État Kubernetes ==="'
            sh 'kubectl get pods -n devops || true'
            sh 'kubectl get svc -n devops || true'
        }
        success {
            echo ' Pipeline terminé avec succès.'
        }
        failure {
            echo ' Pipeline échoué.'
        }
    }
}
