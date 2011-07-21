package engine.classes;

public class Proc {

	private String _stat;
	private int _value;
	private int _duration;
	private String _trigger;
	private int _icd;
	private String _proc_name;
	private boolean _ppm;
	private boolean _proc_chance;
	private boolean _on_crit;
	private int _max_stacks;
	private boolean _can_crit;

	public Proc(String stat, int value, int duration, String trigger, int icd, String proc_name)
	{
		_stat = stat;
		_value = value;
		_duration = duration;
		_trigger = trigger;
		_icd = icd;
		_proc_name = proc_name;
		_ppm = false;
		_proc_chance = false;
		_on_crit = false;
		_max_stacks = 1;
		_can_crit = true;
	}

	public Proc(String stat, int value, int duration, String trigger, int icd, String proc_name,
				boolean ppm, boolean proc_chance, boolean on_crit, int max_stacks, boolean can_crit)
	{
		_stat = stat;
		_value = value;
		_duration = duration;
		_trigger = trigger;
		_icd = icd;
		_proc_name = proc_name;
		_ppm = ppm;
		_proc_chance = proc_chance;
		_on_crit = on_crit;
		_max_stacks = max_stacks;
		_can_crit = can_crit;
	}
	
	public void setStat(String value)
	{
		this._stat = value;
	}
	public String stat()
	{
		return this._stat;
	}
	public void setValue(int value)
	{
		this._value = value;
	}
	public int value()
	{
		return this._value;
	}
	public void setDuration(int value)
	{
		this._duration = value;
	}
	public int duration()
	{
		return this._duration;
	}
	public void setTrigger(String value)
	{
		this._trigger = value;
	}
	public String trigger()
	{
		return this._trigger;
	}
	public void setIcd(int value)
	{
		this._icd = value;
	}
	public int icd()
	{
		return this._icd;
	}
	public void setProcName(String value)
	{
		this._proc_name = value;
	}
	public String proc_name()
	{
		return this._proc_name;
	}
	public void setPpm(boolean value)
	{
		this._ppm = value;
	}
	public boolean ppm()
	{
		return this._ppm;
	}
	public void setProcChance(boolean value)
	{
		this._proc_chance = value;
	}
	public boolean proc_chance()
	{
		return this._proc_chance;
	}
	public void setOnCrit(boolean value)
	{
		this._on_crit = value;
	}
	public boolean on_crit()
	{
		return this._on_crit;
	}
	public void setMaxStacks(int value)
	{
		this._max_stacks = value;
	}
	public int max_stacks()
	{
		return this._max_stacks;
	}
	public void setCanCrit(boolean value)
	{
		this._can_crit = value;
	}
	public boolean can_crit()
	{
		return this._can_crit;
	}

	
	public boolean procs_off_auto_attacks()
	{
		return true;
	}
}

//
//class Proc(object):trigger
//    def __init__(self, stat, value, duration, trigger, icd, proc_name, ppm=False, proc_chance=False, on_crit=False, max_stacks=1, can_crit=True):
//        self.stat = stat
//        self.value = value
//        self.can_crit = can_crit
//        self.duration = duration
//        self.proc_chance = proc_chance
//        self.trigger = trigger
//        self.icd = icd
//        self.max_stacks = max_stacks
//        self.on_crit = on_crit
//        self.proc_name = proc_name
//        self.ppm = ppm
//	
//	trigger_map = {
//		off_auoto_attacks:		'all_attacks', 'auto_attacks', 'all_spells_and_attacks',
//		off_strikes: 			'all_attacks', 'strikes', 'all_spells_and_attacks'
//	}
//
//    def procs_off_auto_attacks(self):
//        return self.trigger in trigger_map[off_auto_attacks]

