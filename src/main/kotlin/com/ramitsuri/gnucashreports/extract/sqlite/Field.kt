package com.ramitsuri.gnucashreports.extract.sqlite

enum class Field(val columnName: String) {
    GUID("guid"),
    NAME("name"),
    TYPE("account_type"),
    PARENT("parent_guid"),
    DESCRIPTION("description"),
    TRANSACTION_GUID("tx_guid"),
    ACCOUNT_GUID("account_guid"),
    VALUE_NUMERATOR("value_num"),
    VALUE_DENOMINATOR("value_denom"),
    POST_DATE("post_date"),
    CURRENCY_GUID("currency_guid"),
    NUM("num"),
}
