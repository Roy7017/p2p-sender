package data;

import java.io.Serializable;

public class Data implements Serializable
{
	private String path;
	private byte[] file;
	private String ext;
	private String name;

	public String getPath()
	{
		return path;
	}

	public void setPath(String path)
	{
		this.path = path;
		setExt(path);
	}

	public byte[] getFile()
	{
		return file;
	}

	public void setFile(byte[] file)
	{
		this.file = file;
	}

	private void setExt(String path)
	{
		String extension = "";

		int i = path.lastIndexOf('.');
		int p = Math.max(path.lastIndexOf('/'), path.lastIndexOf('\\'));

		if (i > p) {
			extension = path.substring(i+1);
		}
		this.ext = extension;
	}

	public String getExt()
	{
		return this.ext;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}
}
