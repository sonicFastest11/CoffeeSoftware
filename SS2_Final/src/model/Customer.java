package model;

import java.text.SimpleDateFormat;
import java.util.regex.Pattern;

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
import model.report.CustomersByNameReport;

public class Customer {
	// static variable to keep track of student id
	public static final String C_id = "id";
	public static final String C_name = "fullName";
	public static final String C_dob = "dob";
	public static final String C_address = "address";
	public static final String C_email = "email";
	public static final String C_rptCustomerByName = "rptCustomerByName";

	private static int idCounter = 0;

	// attributes of importer
	@DAttr(name = C_id, id = true, type = Type.String, auto = true, length = 6, mutable = false, optional = false)
	private String id;

	@DAttr(name = C_name, type = Type.String, length = 20, optional = false)
	private String fullName;

	@DAttr(name = C_dob, type = Type.String, length = 15, optional = false)
	private String dob;

	@DAttr(name = C_address, type = Type.Domain, length = 6, optional = true)
	@DAssoc(ascName = "customer-has-address", role = "customer", ascType = AssocType.One2Many, endType = AssocEndType.Many, associate = @Associate(type = Address.class, cardMin = 1, cardMax = 10))
	private Address address;

	@DAttr(name = C_email, type = Type.String, length = 30, optional = false)
	private String email;

	@DAttr(name = C_rptCustomerByName, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private CustomersByNameReport rptCustomerByName;

	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	@DOpt(type = DOpt.Type.RequiredConstructor)
	public Customer(@AttrRef("fullName") String fullName, @AttrRef("dob") String dob,
			@AttrRef("address") Address address, @AttrRef("email") String email) {
		this(null, fullName, dob, address, email);

	}

	// a shared constructor that is invoked by other constructors
	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public Customer(@AttrRef("id") String id, @AttrRef("fullName") String fullName, @AttrRef("dob") String dob,
			@AttrRef("address") Address address, @AttrRef("email") String email) throws ConstraintViolationException {
		// generate an id
		this.id = nextID(id);
		this.fullName = fullName;
		this.dob = dob;
		this.address = address;
		this.email = email;
	}

	public String getId() {
		return id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getDob() {
		if (validateDOB(dob) == true) {
			return dob;
		}
		throw new ConstraintViolationException(Code.INVALID_VALUE_NOT_SPECIFIED_WHEN_REQUIRED);
	}

	public void setDob(String dob) {
		if (validateDOB(dob) == true) {
			this.dob = dob;
		} else {
			throw new ConstraintViolationException(Code.INVALID_VALUE_NOT_SPECIFIED_WHEN_REQUIRED);
		}
	}

	public boolean validateDOB(String dob) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
		formatter.setLenient(false);
		try {
			formatter.parse(dob.trim());
		} catch (Exception e) {
			System.err.println("Invalid dob : " + dob);
			return false;

		}
		return true;
	}

	public Address getAddress() {
		return address;
	}

	public void setAddress(Address address) {
		this.address = address;
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

	public CustomersByNameReport getRptCustomerByName() {
		return rptCustomerByName;
	}

	// override toString
	/**
	 * @effects returns <code>this.id</code>
	 */
	@Override
	public String toString() {
		return "Customer(" + id + "," + fullName + "," + dob + "," + address + "," + email + ")";
	}

	// automatically generate the next student id
	public String nextID(String id) throws ConstraintViolationException {
		if (id == null) { // generate a new id
			idCounter++;

			return "Cus" + idCounter;
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
