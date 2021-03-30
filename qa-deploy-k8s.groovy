import groovy.json.JsonSlurper

pipeline {
    agent any 
    environment { 
        PROJECT         = 'projeto-final'
        PROJECT_TEST    = 'TDD-BDD'
        REPO_K8S        = 'Templates-K8S'
    }
    
    stages {
        stage ('Clean Workspace'){
            steps{
                deleteDir()
            }
        }
        stage('Git Clone') {
            steps {
                dir ("${PROJECT}"){
                    git branch: 'qa', credentialsId: 'Github', url: "git@github.com:lucasvscosta96/${PROJECT}.git"
                }
                dir ("${PROJECT_TEST}"){
                    git branch: 'main', credentialsId: 'Github', url: "git@github.com:lucasvscosta96/${PROJECT_TEST}.git"
                }
                dir ("${REPO_K8S}"){
                    git branch: 'main', credentialsId: 'Github', url: "git@github.com:lucasvscosta96/${REPO_K8S}.git"
                }
            }
        }
        stage('Build') { 
            steps {
                sh 'cd $PROJECT && \
                    ./mvnw package -Dmaven.test.skip -DskipTests -Dmaven.javadoc.skip=true'
            }
        }
        stage('TDD') { 
            steps {
                sh 'cd $PROJECT_TEST && \
                    ./test.sh'
            }
        }
        stage('BDD') { 
            steps {
                sh 'cd $PROJECT && \
                        nohup ./start.sh & '
                sh 'cd $PROJECT_TEST && \
                        ./test.sh'
            }
        }
        stage('Docker Push') {
            steps {
                sh 'cd $PROJECT && \
                    docker build -t lucasvscosta/qa-projeto-final -f Dockerfile .'
                sh 'docker push lucasvscosta/qa-projeto-final'
            }
        }
        stage('Deploy to QA') {
            steps {
                sh 'cd $REPO_K8S && \
                    cd qa-$PROJECT && \
                    kubectl apply -f .'
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}