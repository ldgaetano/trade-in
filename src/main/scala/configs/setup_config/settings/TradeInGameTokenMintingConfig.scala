package configs.setup_config.settings

case class TradeInGameTokenMintingConfig(
                                          gameTokenName: String,
                                          gameTokenDescription: String,
                                          gameTokenAmount: Long,
                                          gameTokenDecimals: Int,
                                          gameTokenPictureFileName: String,
                                          gameTokenPictureLink: String
                                        )
{}