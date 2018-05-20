node('maven') {
  def checkoutFolder = "/tmp/workspace/$env.JOB_NAME"

  def parksmapFolder = "$checkoutFolder/parksmap"
  def nationalparksFolder = "$checkoutFolder/nationalparks"
  def mlbparksFolder = "$checkoutFolder/mlbparks"

  // get annotated version to make sure every build has a different one
  def appVersion = getAppVersion(parksmapFolder)
  def settingsFilename = null
  def mavenMirrorUrl = 'http://nexus.demo-cicd.svc:8081/repository/maven-all-public/'
  def sonarUrl = 'http://sonarqube.demo-cicd.svc:9000'
  def sonarToken = '29c8f656bcf05f4f134273e697e856ed8536f83f'

  def parksmapBinaryArtifact = null
  def nationalparksBinaryArtifact = null
  def mlbparksBinaryArtifact = null

  // Start session with the service account jenkins which is the one configured by default for this builder
  openshift.withCluster() {
    stage('Checkout code') {
      // Set explicitely the checkout folder for further references
      dir(checkoutFolder) {
        // checkout the source code using the git information provided by the github webhook
        checkout scm
      }
    }
    stage('Create settings file') {
      settingsFilename = prepareEnvironment(checkoutFolder, mavenMirrorUrl)
    }
    stage('Set annotated version') {
      setAppVersion(parksmapFolder, appVersion, settingsFilename)
      setAppVersion(nationalparksFolder, appVersion, settingsFilename)
      setAppVersion(mlbparksFolder, appVersion, settingsFilename)
    }
    stage('Parks Map - Building') {
      build(parksmapFolder, settingsFilename)
      parksmapBinaryArtifact = getBinaryArtifact(parksmapFolder)
    }
    stage('Parks Map - Running unit tests') {
      runUnitTests(parksmapFolder, settingsFilename, sonarUrl, sonarToken)
    }
    stage('National Parks - Building') {
      build(nationalparksFolder, settingsFilename)
      nationalparksBinaryArtifact = getBinaryArtifact(nationalparksFolder)
    }
    stage('National Parks - Running unit tests') {
      runUnitTests(nationalparksFolder, settingsFilename, sonarUrl, sonarToken)
    }
    stage('MLB Parks - Building') {
      build(mlbparksFolder, settingsFilename)
      nationalparksBinaryArtifact = getBinaryArtifact(mlbparksFolder)
    }
    stage('MLB Parks - Running unit tests') {
      runUnitTests(mlbparksFolder, settingsFilename, sonarUrl, sonarToken)
    }
}

def getAppVersion(def appFolder) {
  // Gets the app version from the git repo, not the pom file or any other resources from the application itself.
  dir(appFolder) {
    def appVersion = sh script: "git describe 2> /dev/null || echo '0.0.0-no-tags'", returnStdout: true
    return appVersion.trim()
  }
}

def prepareEnvironment(def folder, def mavenMirrorUrl) {
  def filename = 'temp_settings.xml'
  dir (folder) {
    sh """
      echo "<?xml version='1.0'?><settings><mirrors><mirror><id>nexus-maven-mirror</id><name>Nexus Maven Mirror</name><url>$mavenMirrorUrl</url><mirrorOf>*</mirrorOf></mirror></mirrors></settings>" > $filename
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

def getBinaryArtifact(def appFolder) {
  return sh(script: "ls $appFolder/target/*.jar", returnStdout: true).trim()
}

def getBaseImageName() {
  return 'registry.access.redhat.com/redhat-openjdk-18/openjdk18-openshift:1.3'
}
