package distfilesys.system.dto;

public class PriorityDTO implements DTO {

	private static final long serialVersionUID = -2321725060886524750L;

	private Double priority;

	public PriorityDTO(double priority) {
		setPriority(priority);
	}

	public Double getPriority() {
		return priority;
	}

	public void setPriority(Double priority) {
		this.priority = priority;
	}

}
