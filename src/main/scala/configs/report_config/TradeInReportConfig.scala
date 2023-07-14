package configs.report_config

import com.google.gson.{Gson, GsonBuilder}

import java.io.{File, FileReader, FileWriter}
import scala.util.Try

case class TradeInReportConfig(
                              var gameTokenIssuanceBox: GameTokenIssuanceBoxConfig,
                              var gameLPIssuanceBox: GameLPIssuanceBoxConfig,
                              var gameLPBox: GameLPBoxConfig,
                              var cardValueMappingIssuanceBoxes: Array[CardValueMappingIssuanceBoxConfig],
                              var cardValueMappingBox: CardValueMappingBoxConfig,
                              var playerProxyBox: PlayerProxyBoxConfig
                              ) {

}

object TradeInReportConfig {

  def load(configFilePath: String): Try[TradeInReportConfig] = Try {

    // Load the file
    val configFile: File = new File(configFilePath)

    // Read the file
    val configReader: FileReader = new FileReader(configFile)

    // Create Gson object to parse json
    val gson: Gson = new GsonBuilder().create()

    // Parse the json and create the config object
    val config: TradeInReportConfig = gson.fromJson(configReader, classOf[TradeInReportConfig])
    configReader.close()
    config

  }

  def write(configFilePath: String, reportConfig: TradeInReportConfig): Try[Unit] = Try {

    // Load the file
    val configFile: File = new File(configFilePath)

    // Create Gson object to parse json
    val gson: Gson = new GsonBuilder().setPrettyPrinting().create()

    // Convert new object to json string
    val jsonString = gson.toJson(reportConfig)

    // File Writer
    val configWriter: FileWriter = new FileWriter(configFile)

    // Write the json string
    configWriter.write(jsonString)
    configWriter.close()


  }

}
