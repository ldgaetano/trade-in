package configs.mapping_config

import com.google.gson.{Gson, GsonBuilder}

import java.io.{File, FileReader}
import scala.util.Try

case class TradeInMappingConfig(
                               cardValueMapping: Array[TradeInCardValueMap]
                               ) {}
object TradeInMappingConfig {

  def load(configFilePath: String): Try[TradeInMappingConfig] = Try {

    // Load the file
    val configFile: File = new File(configFilePath)

    // Read the file
    val configReader: FileReader = new FileReader(configFile)

    // Create Gson object to parse json
    val gson: Gson = new GsonBuilder().create()

    // Parse the json and create the config object
    val config: TradeInMappingConfig = gson.fromJson(configReader, classOf[TradeInMappingConfig])
    configReader.close()
    config

  }

}