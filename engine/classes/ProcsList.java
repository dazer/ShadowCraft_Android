package classes;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import core.InvalidInputException;

/**
 * Proc container. Instantiates procs and defines methods to filter them.
 */
public class ProcsList {

    @SuppressWarnings("serial")
    class InvalidProcException extends InvalidInputException {
        public InvalidProcException(String message) {
            super(message);
        }
    }

    private static final Set<String> allowed_procs = Data.proc_data.keySet();
    private Map<String, Proc> present_procs = new HashMap<String, Proc>();

    public ProcsList(String... args) {
        for (String proc : args) {
            this.append_proc(proc);
        }
    }

    public HashMap<String, ?> get_proc_data(String arg) throws InvalidProcException {
        if (!allowed_procs().contains(arg))
            throw new InvalidProcException(String.format("No data available for proc %s", arg));
        return Data.proc_data.get(arg);
    }

    public void append_proc(String proc) {
        this.present_procs.put(proc, new Proc(get_proc_data(proc)));
    }

    public Proc get_proc(String proc) {
        return this.present_procs.get(proc);
    }

    public void del_proc(String proc) {
        this.present_procs.remove(proc);
    }

    public boolean exists_proc(String proc) {
        return this.present_procs.containsKey(proc);
    }

    public Set<Proc> get_all_procs_for_stat(String stat) {
        Set<Proc> procs = new HashSet<Proc>();
        for (String proc_name : allowed_procs) {
            Proc proc = get_proc(proc_name);
            if (proc != null && (proc.stat().equals(stat) || stat == null)) {
                procs.add(proc);
            }
        }
        return procs;
    }

    public Set<Proc> get_all_procs_for_stat() {
        return get_all_procs_for_stat(null);
    }

    public Set<Proc> get_all_damage_procs() {
        Set<Proc> procs = new HashSet<Proc>();
        for (String proc_name : allowed_procs) {
            Proc proc = get_proc(proc_name);
            if (proc != null && (proc.stat().equals("spell_damage") || proc.stat().equals("physical_damage")))
                procs.add(proc);
        }
        return procs;
    }

    public static Set<String> allowed_procs() {
        return allowed_procs;
    }

}
