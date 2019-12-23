package org.realitix.dfilesearch.filesearch.configuration;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;

import javax.validation.constraints.NotEmpty;

/**
 * Configuration for FileSearchExecutor
 * Strictly adhere to the guidelines given in docs: https://www.dropwizard.io/en/stable/getting-started.html
 */
public class FileExecutorConfiguration extends Configuration {

    @NotEmpty
    private String name;

    private Client ports;

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    public Client getPorts() {
        return ports;
    }

    public void setPorts(Client ports) {
        this.ports = ports;
    }
}
