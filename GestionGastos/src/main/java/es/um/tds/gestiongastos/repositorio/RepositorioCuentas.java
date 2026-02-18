package es.um.tds.gestiongastos.repositorio;

import es.um.tds.gestiongastos.modelo.CuentaCompartida;
import es.um.tds.gestiongastos.modelo.Persona;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Repositorio de cuentas compartidas
 * Implementa el patrón Singleton para garantizar una única instancia.
 */
public class RepositorioCuentas {
	
	private static RepositorioCuentas instance;
	
	private List<CuentaCompartida> accounts;
	
	private RepositorioCuentas() {
		this.accounts = new ArrayList<>();
	}
	
	public static synchronized RepositorioCuentas getInstance() {
		if (instance == null) {
			instance = new RepositorioCuentas();
		}
		
		return instance;
	}
	
	public List<CuentaCompartida> getTotalCuentas(){
		return new ArrayList<>(accounts);
	}
	
	public Optional<CuentaCompartida> getCuentaByID(int _ID){
		return accounts.stream()
				.filter(c -> c.getID() == _ID)
				.findFirst();
	}
	
	public Optional<CuentaCompartida> getCuentaByNombre(String _name){
		if (_name == null) return Optional.empty();
		
		return accounts.stream()
				.filter(c -> c.getNombre().equalsIgnoreCase(_name.trim()))
				.findFirst();
	}
	
	/**
	* Guarda una cuenta (añade si es nueva).
	* @param _account Cuenta a guardar
	* @throws IllegalArgumentException si la cuenta es null
	*/
	public void addCuenta(CuentaCompartida _account) {
		if (_account == null) {
			throw new IllegalArgumentException("La cuenta no puede ser null");
		}
		
		if (getCuentaByID(_account.getID()).isEmpty()) {
			accounts.add(_account);
		}
	}
	
	public boolean removeCuenta(CuentaCompartida _account) {
		if (_account == null) {
			return false;
		}
		
		return accounts.remove(_account);
	}

	public boolean deleteByID(int _ID) {
		Optional<CuentaCompartida> account = getCuentaByID(_ID);
		return account.map(this::removeCuenta).orElse(false);
	}
	
	public List<CuentaCompartida> getCuentasByPersona(Persona _person){
		if (_person == null) {
			return new ArrayList<>();
		}
		
		return accounts.stream()
				.filter(c -> c.contienePersona(_person))
				.collect(Collectors.toList());
	}
	
	public boolean existsCuentaByNombre(String _name) {
		return getCuentaByNombre(_name).isPresent();
	}
	
	public int count() {
		return accounts.size();
	}
	
	public void reset() {
		accounts.clear();
		CuentaCompartida.resetContador();
	}
	
	public static synchronized void resetInstance() {
		instance = null;
	}
}
