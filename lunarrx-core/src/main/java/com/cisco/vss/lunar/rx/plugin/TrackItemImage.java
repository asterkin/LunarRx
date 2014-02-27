package com.cisco.vss.lunar.rx.plugin;

/**
 * The track item image holds the metadata and data of an image that is passed on a track item.
 */
public class TrackItemImage
{
    private int    height;
    private int    width;
    private String mime;
    private byte[] data;    
  
    
    /**
     * Create a new track item image with the given metadata and data.
     * @param height The height of the image
     * @param width The width of the image
     * @param mime The mime type of the image (i.e. jpg, png, etc).
     * @param data The bytes of the image.
     */
    public TrackItemImage(int height, int width, String mime, byte[] data)
    {
        this.height = height;
        this.width  = width;
        this.mime   = mime;
        this.data   = data;
    }
    
    /**
     * Returns the height of the image
     * @return the height of the image
     */
    public int getHeight()
    {
        return height;
    }
    
    /** 
     * Returns the width of the image.
     * @return the width of the image.
     */
    public int getWidth()
    {
        return width;
    }
    
    /**
     * Returns the mime type of the image.
     * @return the mime type of the image.
     */
    public String getMime()
    {
        return mime;
    }
    
    /**
     * Returns the binary data of the image in a byte array.
     * @return the binary data of the image in a byte array.
     */
    public byte[] getData()
    {
        return data;
    }
}
