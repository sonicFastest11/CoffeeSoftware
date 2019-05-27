package model;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.exceptions.ConstraintViolationException.Code;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.util.Tuple;
import model.report.SuppliersByNameReport;

public class Supplier {
	// static variable to keep track of student id
	public static final String S_id = "id";
	public static final String S_name = "supplierName";
	public static final String S_phone = "phone";
	public static final String S_email = "email";
	public static final String S_address = "address";
	public static final String S_rptSupplierByName = "rptSupplierByName";
	private static int idCounter = 0;

	// attributes of importer
	@DAttr(name = S_id, id = true, type = Type.String, auto = true, length = 6, mutable = false, optional = false)
	private String id;

	@DAttr(name = S_name, type = Type.String, length = 15, optional = false)
	private String supplierName;

	@DAttr(name = S_phone, type = Type.String, length = 15, optional = false)
	private String phone;

	@DAttr(name = S_email, type = Type.String, length = 30, optional = false)
	private String email;
	
	@DAttr(name = S_address, type = Type.Domain, length =6, optional = true)
	@DAssoc(ascName = "supplier-has-address", role = "supplier", ascType = AssocType.One2Many, endType = AssocEndType.Many, associate = @Associate(type = Address.class, cardMin = 1, cardMax = 10))
	private Address address;
	
	@DAttr(name=S_rptSupplierByName,type=Type.Domain, serialisable=false, 
		      // IMPORTANT: set virtual=true to exclude this attribute from the object state
		      // (avoiding the view having to load this attribute's value from data source)
		      virtual=true)
		  private SuppliersByNameReport rptSupplierByName;

	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	@DOpt(type = DOpt.Type.RequiredConstructor)
	public Supplier(@AttrRef("supplierName") String supplierName, @AttrRef("phone") String phone,
			 @AttrRef("email") String email, @AttrRef("address") Address address) {
		this(null, supplierName, phone, email, address);
	}

	// a shared constructor that is invoked by other constructors
	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public Supplier(@AttrRef("id") String id, @AttrRef("supplierName")String supplierName, @AttrRef("phone") String phone,@AttrRef("email") String email, @AttrRef("address") Address address) throws ConstraintViolationException {
		// generate an id
		this.id = nextID(id);

		// assign other values
		this.supplierName = supplierName;
		this.phone = phone;
		this.email = email;
		this.address = address;

	}

	public String getId() {
		return id;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}


	public String getEmail() throws ConstraintViolationException {
		if (isValid(email) == true) {
			return email;
		}
		throw new ConstraintViolationException(Code.INVALID_VALUE_NOT_SPECIFIED_WHEN_REQUIRED);

	}

	public void setEmail(String email) {
		if (isValid(email) == true) {
			this.email = email;
		} else {
			throw new ConstraintViolationException(Code.INVALID_VALUE_NOT_SPECIFIED_WHEN_REQUIRED);
		}
	}
	public boolean isValid(String email) {
		if (!email.contains("@gmail.com")) {
			return false;
		} else {
			return true;
		}
	}
	public Address getAddress() {
		return address;
	}
	
	public void setAddress(Address address) {
		this.address = address;
	}

	public SuppliersByNameReport getRptSupplierByName() {
		return rptSupplierByName;
	}

	// override toString
	/**
	 * @effects returns <code>this.id</code>
	 */
	@Override
	public String toString() {
		return "Supplier(" + id + "," + supplierName + "," + phone + "," + address + "," + email + ")";
	}

	// automatically generate the next student id
	public String nextID(String id) throws ConstraintViolationException {
		if (id == null) { // generate a new id
			idCounter++;

			return "SUP" + idCounter;
		} else {
			// update id
			int num;
			try {
				num = Integer.parseInt(id.substring(3));
			} catch (RuntimeException e) {
				throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, e,
						new Object[] { id });
			}

			if (num > idCounter) {
				idCounter = num;
			}

			return id;
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

			String maxId = (String) maxVal;

			try {
				int maxIdNum = Integer.parseInt(maxId.substring(3));

				if (maxIdNum > idCounter) // extra check
					idCounter = maxIdNum;

			} catch (RuntimeException e) {
				throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, e,
						new Object[] { maxId });
			}
		}
	}

}
