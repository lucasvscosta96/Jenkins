import groovy.json.JsonSlurper

pipeline {
    agent any 
    environment { 
        PROJECT         = 'projeto-final'
        PROJECT_TEST    = 'TDD-BDD'
        REPO_HEROKU     = 'dev-projeto-final-d'
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
                    git branch: 'dev', credentialsId: 'Github', url: "git@github.com:lucasvscosta96/${PROJECT}.git"
                }
                dir ("${PROJECT_TEST}"){
                    git branch: 'main', credentialsId: 'Github', url: "git@github.com:lucasvscosta96/${PROJECT_TEST}.git"
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
        stage('Deploy to Heroku') {
            steps {
                sh 'cd $PROJECT && \
                    heroku git:remote -a $REPO_HEROKU && \
                    git push heroku $GIT_BRANCH:master'
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}