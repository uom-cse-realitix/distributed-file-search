package org.realitix.dfilesearch.filesearch.util;

public interface RequestParser<T> {

    /**
     * parsing peer messages
     * @param t message
     * @return response for peer message
     */
    T parse(T t);

}
