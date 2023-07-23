package configs.setup_config.settings

case class TradeInSetCreationMutliSigConfig(
                                          threshold: Int,
                                          addresses: Array[String]
                                        )
{}