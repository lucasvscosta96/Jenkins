import groovy.json.JsonSlurper

pipeline {
    agent any 
    environment { 
        PROJECT         = 'projeto-final'
        PROJECT_TEST    = 'TDD-BDD'
        REPO_K8S        = 'Templates-K8S'
        CONTEXT_K8S     = 'projeto-final-concrete'
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
                    git branch: 'main', credentialsId: 'Github', url: "git@github.com:lucasvscosta96/${PROJECT}.git"
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
                    ./mvnw clean && \
                    ./mvnw package -Dmaven.test.skip -DskipTests -Dmaven.javadoc.skip=true'
            }
        }
        stage('Docker Push') {
            steps {
                sh 'cd $PROJECT && \
                    docker build -t lucasvscosta/prod-projeto-final -f Dockerfile .'
                sh 'docker push lucasvscosta/prod-projeto-final'
            }
        }
        stage('Deploy to Prod'){
            steps {
                sh 'cd $REPO_K8S && \
                    cd aks/prod && \
                    kubectl config use-context $CONTEXT_K8S && \
                    kubectl delete deploy prod-$PROJECT && \
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