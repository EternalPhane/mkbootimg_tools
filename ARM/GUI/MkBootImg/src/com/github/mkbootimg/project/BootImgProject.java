package com.github.mkbootimg.project;

public class BootImgProject {
	private String workspace;
	private String bootImg;
	private String bootFolder;
	private String newBootImg;

	public BootImgProject(String workspace, String bootImg) {
		this.setWorkspace(workspace);
		this.setBootImg(bootImg);
	}

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getBootImg() {
		return bootImg;
	}

	public void setBootImg(String bootImg) {
		this.bootImg = bootImg;
	}

	public String getBootFolder() {
		return bootFolder;
	}

	public void setBootFolder(String bootFolder) {
		this.bootFolder = bootFolder;
	}

	public String getNewBootImg() {
		return newBootImg;
	}

	public void setNewBootImg(String newBootImg) {
		this.newBootImg = newBootImg;
	}
	
	/**
	 * Splits the boot image.
	 */
	public void splitBootImg() {
		// TODO: Complete this method after converting ARM/wrapper to java class.
	}
}
