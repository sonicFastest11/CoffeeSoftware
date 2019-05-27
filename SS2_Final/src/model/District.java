package model;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.util.Tuple;

public class District {
	@DAttr(name = "id", id = true, auto = true, length = 3, mutable = false, optional = false, type = Type.Integer)
	private int id;
	private static int idCounter;

	
	
	@DAttr(name = "district", type = Type.String, length = 20, optional = false)
	private String district;
	
	
	// from object form: Student is not included
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	@DOpt(type = DOpt.Type.RequiredConstructor)
	public District(@AttrRef("district") String district) {
		this(null, district);
	}
	

	// based constructor (used by others)
	@DOpt(type = DOpt.Type.DataSourceConstructor)

	public District(Integer id, String district) {
		this.id = nextId(id);
		this.district = district;
	}

	private static int nextId(Integer currID) {
		if (currID == null) {
			idCounter++;
			return idCounter;
		} else {
			int num = currID.intValue();
			if (num > idCounter)
				idCounter = num;

			return currID;
		}
	}

	/**
	 * @requires minVal != null /\ maxVal != null
	 * @effects update the auto-generated value of attribute <tt>attrib</tt>,
	 *          specified for <tt>derivingValue</tt>, using <tt>minVal, maxVal</tt>
	 */
	@DOpt(type = DOpt.Type.AutoAttributeValueSynchroniser)
	public static void updateAutoGeneratedValue(DAttr attrib, Tuple derivingValue, Object minVal, Object maxVal)
			throws ConstraintViolationException {

		if (minVal != null && maxVal != null) {
			// TODO: update this for the correct attribute if there are more than one auto
			// attributes of this class
			int maxIdVal = (Integer) maxVal;
			if (maxIdVal > idCounter)
				idCounter = maxIdVal;
		}
	}

	public int getId() {
		return id;
	}




	public String getDistrict() {
		return district;
	}

	public void setDistrict(String district) {
		this.district = district;
	}

	@Override
	public String toString() {
		return district;
	}
}
