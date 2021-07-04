/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.sybit.airtableandroid.vo;


import javadz.beanutils.PropertyUtils;

/**
 *
 * @author fzr
 */
public class PostRecord<T> {
    
    private T fields;

    /**
     * @return the fields
     */
    public T getFields() {
        return fields;
    }

    /**
     * @param item the fields to set
     */
    public void setFields(T item) {
        this.fields = item;
        /*

            Field[] attributes = item.getClass().getDeclaredFields();
            for (Field attribute : attributes) {
                String name = attribute.getName();
                if (name.equals("id") || name.equals("createdTime")) {

                }

                 //   List<Attachment> obj = (List<Attachment>) BeanUtilsBean.getInstance().getPropertyUtils().getProperty(item, "photos");

            }
            */

    }

    /**
     * Check if writable property exists.
     *
     * @param bean bean to inspect
     * @param property name of property
     * @return true if writable property exists.
     */
    private static boolean propertyExists (Object bean, String property) {
        return PropertyUtils.isReadable(bean, property) &&
                PropertyUtils.isWriteable(bean, property);
    }



}
