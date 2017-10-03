package distfilesys.system.election;

public class DefaultElection implements Election {

	public double calculate() {
		int proc = Runtime.getRuntime().availableProcessors();
		double mem = Runtime.getRuntime().maxMemory();
		double valor = (10 * mem / proc) / mem;
		return valor;
	}

}
