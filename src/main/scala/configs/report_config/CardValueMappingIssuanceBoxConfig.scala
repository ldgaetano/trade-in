package configs.report_config

case class CardValueMappingIssuanceBoxConfig(
                                            var cardValueMappingIssuanceContract: String,
                                            var cardValueMappingSingletonTokenId: String,
                                            var boxId: String,
                                            var txId: String
                                            ) {

}
