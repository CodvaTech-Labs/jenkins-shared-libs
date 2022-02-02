pipeline {
    agent any

    stages {
        stage('URL_Monitoring') {
            steps {
                 sh '''#!/bin/bash
                  url=$URL
                  attempts=2
                  timeout=5
                  online=false
                  echo "Checking status of $url."
                  for (( i=1; i<=$attempts; i++ ))
                  do
                    code=`curl -sL --connect-timeout 20 --max-time 30 -w "%{http_code}\\\\n" "$url" -o /dev/null`
                    echo "Found code $code for $url."
                    if [ "$code" = "200" ]; then
                      echo "Website $url is online."
                      online=true
                      break
                    else
                      echo "Website $url seems to be offline. Waiting $timeout seconds."
                      sleep $timeout
                    fi
                  done
                  if $online; then
                    echo "Monitor finished, website is online."
                    exit 0
                  else
                    echo "Monitor failed, website seems to be down."
                    exit 1
                  fi'''
            }
        }

      stage('Slack Message') {
      steps {
        slackSend channel: '#devops-alerts',
            color: 'good',
            message: "*${currentBuild.currentResult}:* Job ${env.JOB_NAME} build ${env.BUILD_NUMBER}\n More info at: ${env.BUILD_URL}"
      }
    }
    }
}