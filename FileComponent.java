package com.plm.component;

import com.ptc.core.components.rendering.guicomponents.HTMLGuiComponent;

public class FileComponent extends HTMLGuiComponent {

	

	private String value;
    private int width;
    private int maxLength;
    private int maxByteLength;
    private boolean hidden;
    private String inputType;
   
	private String id;
    
    public FileComponent() {
        this.width = 10;
        this.maxLength = 0;
        this.maxByteLength = 0;
        this.hidden = false;
        this.inputType = "file";
        this.id="file";
        this.setRenderer(new FileComponentRenderer());
        this.setComponentType("___file");
    }
    public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

    public String getValue() {
        return this.value;
    }
    
    public void setValue(final String value) {
        this.value = value;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void setWidth(final int width) {
        this.width = width;
    }
    
    public boolean isHidden() {
        return this.hidden;
    }
    
    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }
    
    @Override
    public Comparable getInternalValue() {
        return this.value;
    }
    
    public String getInputType() {
        return this.inputType;
    }
    
    public void setInputType(final String inputType) {
        this.inputType = inputType;
    }
    
    public int getMaxLength() {
        return this.maxLength;
    }
    
    public int getMaxByteLength() {
        return this.maxByteLength;
    }
    
    public void setMaxLength(final int maxLength) {
        this.maxLength = maxLength;
    }
    
    public void setMaxByteLength(final int maxByteLength) {
        this.maxByteLength = maxByteLength;
    }
}
