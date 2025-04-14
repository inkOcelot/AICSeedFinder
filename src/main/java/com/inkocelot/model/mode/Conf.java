package com.inkocelot.model.mode;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "mode"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SingleThreadConf.class, name = "1"),
        @JsonSubTypes.Type(value = SlidingWindowMappedConf.class, name = "2")
})
public abstract class Conf {
}
