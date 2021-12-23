package org.ebics.client.order

interface IOrderTypeDefinition {
    val adminOrderType: EbicsAdminOrderType
}

interface IOrderTypeDefinition25 : IOrderTypeDefinition {
    val businessOrderType: String?
}

interface IOrderTypeDefinition30: IOrderTypeDefinition {
    val service: IEbicsService?
}