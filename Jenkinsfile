node('maven') {
  try {
    def checkoutFolder = "/tmp/workspace/$env.JOB_NAME"

    def parksmapFolder = "$checkoutFolder/parksmap"
    def nationalparksFolder = "$checkoutFolder/nationalparks"
    def mlbparksFolder = "$checkoutFolder/mlbparks"

    def openshiftCicdProjectName = 'demo-cicd'
    def openshiftDevProjectName = 'parksmap-dev'
    def openshiftTestProjectName = 'parksmap-test'
    def openshiftLiveProjectName = 'parksmap-live'

    // get annotated version to make sure every build has a different one
    def appVersion = null
    def settingsFilename = null
    def nexusServerUrl = 'http://nexus.' + openshiftCicdProjectName + '.svc:8081/'
    def mavenMirrorUrl = nexusServerUrl + 'repository/maven-all-public/'
    def hostedMavenUrl = nexusServerUrl + 'repository/maven-releases/'
    def dockerRegistryUrl = nexusServerUrl + "repository/mitzi/$openshiftLiveProjectName/"
    def openshiftRegistryUrl = 'docker-registry.default.svc:5000/' + openshiftCicdProjectName + '/'
    def nexusUsername = 'admin'
    def nexusPassword = 'admin123'
    def sonarUrl = 'http://sonarqube.' + openshiftCicdProjectName + '.svc:9000'
    def sonarToken = '29c8f656bcf05f4f134273e697e856ed8536f83f'

    def parksmapBinaryArtifact = null
    def nationalparksBinaryArtifact = null
    def mlbparksBinaryArtifact = null

    def imageStreamsPreffix = "$env.JOB_NAME-$env.BUILD_NUMBER"

    // Start session with the service account jenkins which is the one configured by default for this builder
    openshift.withCluster() {
      stage('Checkout code') {
        // Set explicitely the checkout folder for further references
        dir(checkoutFolder) {
          // checkout the source code using the git information provided by the github webhook
          // This syntax allows to checkout also all annotated tags so get can get a different version each time.
          checkout([
              $class: 'GitSCM',
              branches: scm.branches,
              doGenerateSubmoduleConfigurations: scm.doGenerateSubmoduleConfigurations,
              extensions: [[$class: 'CloneOption', noTags: false, shallow: false, depth: 0, reference: '']],
              userRemoteConfigs: scm.userRemoteConfigs,
           ])
        }
      }
      stage('Create settings file') {
        settingsFilename = prepareEnvironment(checkoutFolder, mavenMirrorUrl, nexusUsername, nexusPassword)
      }
      stage('Get new version') {
        appVersion = getAppVersion(parksmapFolder)
      }
      stage("Parks Map - set version $appVersion") {
        setAppVersion(parksmapFolder, appVersion, settingsFilename)
      }
      stage('Parks Map - Building') {
        build(parksmapFolder, settingsFilename)
        parksmapBinaryArtifact = getBinaryArtifact(parksmapFolder, 'jar')
      }
      stage('Parks Map - Running unit tests') {
        runUnitTests(parksmapFolder, settingsFilename, sonarUrl, sonarToken)
      }
      stage("National Parks - set version $appVersion") {
        setAppVersion(nationalparksFolder, appVersion, settingsFilename)
      }
      stage('National Parks - Building') {
        build(nationalparksFolder, settingsFilename)
        nationalparksBinaryArtifact = getBinaryArtifact(nationalparksFolder, 'jar')
      }
      stage('National Parks - Running unit tests') {
        runUnitTests(nationalparksFolder, settingsFilename, sonarUrl, sonarToken)
      }
      stage("MLB Parks - set version $appVersion") {
        setAppVersion(mlbparksFolder, appVersion, settingsFilename)
      }
      stage('MLB Parks - Building') {
        build(mlbparksFolder, settingsFilename)
        mlbparksBinaryArtifact = getBinaryArtifact(mlbparksFolder, 'war')
      }
      stage('MLB Parks - Running unit tests') {
        runUnitTests(mlbparksFolder, settingsFilename, sonarUrl, sonarToken)
      }

      stage('Parks Map - push jar to Nexus') {
        uploadArtifactToNexus(parksmapFolder, settingsFilename, hostedMavenUrl, parksmapBinaryArtifact)
      }
      stage('National Parls - push jar to Nexus') {
        uploadArtifactToNexus(nationalparksFolder, settingsFilename, hostedMavenUrl, nationalparksBinaryArtifact)
      }
      stage('MLB Parks - push war to Nexus') {
        uploadArtifactToNexus(mlbparksFolder, settingsFilename, hostedMavenUrl, mlbparksBinaryArtifact)
      }

      def parksmapImageStream = null
      def nationalparksImageStream = null
      def mlbparksImageStream = null

      openshift.withProject( openshiftCicdProjectName ) {
        try {
          stage('Parks Map - binary build') {
            def baseImage = getBaseImageName('jar')
            parksmapImageStream = "$imageStreamsPreffix-parksmap"
            doBinaryBuild(parksmapImageStream, baseImage, parksmapBinaryArtifact, appVersion)
          }
          stage('National Parls - binary build') {
            def baseImage = getBaseImageName('jar')
            nationalparksImageStream = "$imageStreamsPreffix-nationalparks"
            doBinaryBuild(nationalparksImageStream, baseImage, nationalparksBinaryArtifact, appVersion)
          }
          stage('MLB Parks - binary build') {
            def baseImage = getBaseImageName('war')
            mlbparksImageStream = "$imageStreamsPreffix-mlbparks"
            doBinaryBuild(mlbparksImageStream, baseImage, mlbparksBinaryArtifact, appVersion)
          }

          // Execute all three next commands in another node with support for skopeo
          node('skopeo') {
            stage('Parks Map - push docker image to Nexus') {
              uploadOcpImageToNexus(openshiftRegistryUrl + parksmapImageStream + ':' + appVersion, dockerRegistryUrl + 'parksmap:' + appVersion, "$nexusUsername:$nexusPassword")
            }
            stage('National Parls - push docker image to Nexus') {
              uploadOcpImageToNexus(openshiftRegistryUrl + nationalparksImageStream + ':' + appVersion, dockerRegistryUrl + 'nationalparks:' + appVersion, "$nexusUsername:$nexusPassword")
            }
            stage('MLB Parks - push docker image to Nexus') {
              uploadOcpImageToNexus(openshiftRegistryUrl + mlbparksImageStream + ':' + appVersion, dockerRegistryUrl + 'mlbparks:' + appVersion, "$nexusUsername:$nexusPassword")
            }
          }
        }
        finally {
          input "Continue?"

          // Clean up local image streams and build configurations if they exist
          openshift.selector( "bc/$parksmapImageStream" ).delete( "--cascade=true", "--ignore-not-found=true" )
          openshift.selector( "bc/$nationalparksImageStream" ).delete( "--cascade=true", "--ignore-not-found=true" )
          openshift.selector( "bc/$mlbparksImageStream" ).delete( "--cascade=true", "--ignore-not-found=true" )

          openshift.selector( "is/$parksmapImageStream" ).delete( "--cascade=true", "--ignore-not-found=true" )
          openshift.selector( "is/$nationalparksImageStream" ).delete( "--cascade=true", "--ignore-not-found=true" )
          openshift.selector( "is/$mlbparksImageStream" ).delete( "--cascade=true", "--ignore-not-found=true" )
        }
      }

      // Single deployment into DEV

      // Ask for manual approval before going to TEST

      // Single deployment into TEST

      // Ask for manual approval before going to LIVE

      // Blue/Green deployment into LIVE

    }
  }
  finally {
    // Place any notification to an external system we need to do in case of success or failure
  }
}

def getAppVersion(def appFolder) {
  // Gets the app version from the git repo, not the pom file or any other resources from the application itself.
  dir(appFolder) {
    def appVersion = sh script: "git describe 2> /dev/null || echo '0.0.0-no-tags'", returnStdout: true
    return appVersion.trim()
  }
}

def prepareEnvironment(def folder, def mavenMirrorUrl, def nexusUsername, def nexusPassword) {
  def filename = 'temp_settings.xml'
  def authSection = "<servers><server><id>nexus-maven-mirror</id><username>$nexusUsername</username><password>$nexusPassword</password></server></servers>"

  dir (folder) {
    sh """
      echo "<?xml version='1.0'?><settings>$authSection<mirrors><mirror><id>nexus-maven-mirror</id><name>Nexus Maven Mirror</name><url>$mavenMirrorUrl</url><mirrorOf>*</mirrorOf></mirror></mirrors></settings>" > $filename
    """
    return "$folder/$filename"
  }
}

def setAppVersion(def appFolder, def appVersion, def settingsFilename) {
  dir (appFolder) {
    sh """
      mvn -s $settingsFilename versions:set versions:commit -DnewVersion="$appVersion"
    """
  }
}

def build(def appFolder, def settingsFilename) {
  dir (appFolder) {
    sh """
      mvn -s $settingsFilename package -DskipTests
    """
  }
}

def runUnitTests(def appFolder, def settingsFilename, def sonarUrl, def sonarToken) {
  dir (appFolder) {
    try {
      sh """
        mvn -s $settingsFilename test -P coverage
      """
    }
    finally {
      junit 'target/*reports/**/*.xml'
      sh """
        mvn -s $settingsFilename sonar:sonar -Dsonar.host.url=$sonarUrl -Dsonar.login=$sonarToken -Dsonar.jacoco.reportPaths=target/coverage-reports/jacoco-ut.exec
      """
    }
  }
}

def getBinaryArtifact(def appFolder, def artifactExtension) {
  return sh(script: "ls $appFolder/target/*.$artifactExtension", returnStdout: true).trim()
}

def getBaseImageName(def artifactExtension) {
  return (artifactExtension == 'jar') ? 'openjdk18-openshift:1.3' : 'eap71-openshift:1.2'
}

def uploadArtifactToNexus(def appFolder, def settingsFilename, def repositoryUrl, def artifactFilename) {
  dir(appFolder) {
    sh """
      mvn -s $settingsFilename deploy:deploy-file -DgeneratePom=false -DpomFile=pom.xml -DrepositoryId=nexus-maven-mirror -Durl=$repositoryUrl -Dfile=$artifactFilename
    """
  }
}

def doBinaryBuild(def imageStream, def baseImage, def binaryArtifact, def appVersion) {
  // Creation of the build config
  openshift.newBuild("--allow-missing-imagestream-tags=true", "--binary=true", "-i '$baseImage'", "--name='$imageStream'", "--to='$imageStream:$appVersion'")
  // Start the binary build
  openshift.startBuild("$imageStream", "--from-file='$binaryArtifact'", "--follow")
}

def uploadOcpImageToNexus(def openshiftStreamTag, def nexusImageStreamTag, def nexusCredentials) {
  def openshiftCredentials = sh(script: "oc whoami -t", returnStdout: true).trim()
  def srcCredentials = 'openshift:\\' + openshiftCredentials
  sh """
    set +x
    skopeo copy --src-tls-verify=false --dest-tls-verify=false --src-creds=$srcCredentials --dest-creds=$nexusCredentials docker://$openshiftStreamTag docker://$nexusImageStreamTag
  """
}
