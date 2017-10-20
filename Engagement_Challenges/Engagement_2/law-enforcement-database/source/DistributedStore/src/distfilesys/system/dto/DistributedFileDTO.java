/*
 * DistributedFileDTO.java
 *
 * Created on 10 de Agosto de 2007, 19:23
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package distfilesys.system.dto;

import distfilesys.system.DistributedFile;

/**
 * 
 * @author Calebe de Paula Bianchini
 */
public class DistributedFileDTO implements DTO {

	private static final long serialVersionUID = 59007917303943286L;
	
	protected DistributedFile distributedFile;

	public DistributedFileDTO(DistributedFile distributedFile) {
		setDistributedFile(distributedFile);
	}
	
	public DistributedFile getDistributedFile() {
		return distributedFile;
	}

	public void setDistributedFile(DistributedFile distributedFile) {
		this.distributedFile = distributedFile;
	}   
}