import groovy.json.JsonSlurper

pipeline {
    agent any 
    environment { 
        PROJECT         = 'projeto-final'
        PROJECT_TEST    = 'TDD-BDD'
        REPO_HEROKU     = 'qa-projeto-final-d'
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
            }
        }
        stage('Build') { 
            steps {
                sh 'cd $PROJECT && \
                    ./mvnw clean && \
                    ./mvnw package -Dmaven.test.skip -DskipTests -Dmaven.javadoc.skip=true'
            }
        }
        stage('Deploy to Heroku') {
            steps {
                sh 'cd $PROJECT && \
                    heroku git:remote -a $REPO_HEROKU && \
                    git push heroku qa:master'
            }
        }
    }
    post {
        always {
            deleteDir()
        }
    }
}