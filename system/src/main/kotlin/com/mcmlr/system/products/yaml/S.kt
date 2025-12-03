package com.mcmlr.system.products.yaml

import com.mcmlr.blocks.api.app.StringResource

enum class S {
    AT,
    CREATED,
    KB,
    FILE_ICON,
    EDITABLE_FILE_ICON,
    MODIFIED,
    OPEN,
    PLUGINS,
    FILES,
    FILE,
    EDIT,
    ERROR_NO_FILE_SELECTED,
    ERROR_FILE,
    DOESNT_EXIST,
    SAVE,
    VALUE_SAVED,
    LIST,

    ;

    fun resource(): StringResource = StringResource("files", this.name)
}