package configs.setup_config

import com.google.gson.{Gson, GsonBuilder}
import configs.setup_config.node.TradeInNodeConfig
import configs.setup_config.settings.TradeInSetttingsConfig

import java.io.{File, FileReader}
import scala.util.Try

case class TradeInSetupConfig(
                        node: TradeInNodeConfig,
                        settings: TradeInSetttingsConfig
                        )

object TradeInSetupConfig {

  def load(configFilePath: String): Try[TradeInSetupConfig] = Try {

    // Load the file
    val configFile: File = new File(configFilePath)

    // Read the file
    val configReader: FileReader = new FileReader(configFile)

    // Create Gson object to parse json
    val gson: Gson = new GsonBuilder().create()

    // Parse the json and create the config object
    val config: TradeInSetupConfig = gson.fromJson(configReader, classOf[TradeInSetupConfig])
    configReader.close()
    config

  }

}
