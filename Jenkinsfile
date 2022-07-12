pipeline {
    agent any

    stages {
        stage('Clean') {
            steps {
                withGradle {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean'
                }
            }
        }
        stage('Build') {
            steps {
                withGradle {
                    sh './gradlew build -x test'
                }
            }
        }
        stage('Test') {
            steps {
                withGradle {
                    sh './gradlew test'
                }
            }
            junit '**/build/test-results/test/*.xml'
        }
    }
}
