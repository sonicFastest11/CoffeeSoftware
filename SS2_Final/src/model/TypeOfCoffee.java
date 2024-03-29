package model;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.util.Tuple;
import model.report.CoffeesByTypeReport;

public class TypeOfCoffee {
	public static final String T_id = "id";
	public static final String T_name = "typeOfCoffee";
	public static final String T_rptCoffeeByType = "rptCoffeeByType";
	private static int idCounter = 0;

	@DAttr(name = T_id, id = true, auto = true, type = Type.String, length = 6, mutable = false, optional = false)
	private String id;

	@DAttr(name = T_name, type = Type.String, length = 10, optional = false)
	private String typeOfCoffee;
	
	@DAttr(name=T_rptCoffeeByType,type=Type.Domain, serialisable=false, 
		      // IMPORTANT: set virtual=true to exclude this attribute from the object state
		      // (avoiding the view having to load this attribute's value from data source)
		      virtual=true)
		  private CoffeesByTypeReport rptCoffeeByType;

	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public TypeOfCoffee(String id, String typeOfCoffee) {
		this.id = nextID(id);
		this.typeOfCoffee = typeOfCoffee;
	}
	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	@DOpt(type = DOpt.Type.RequiredConstructor)
	public TypeOfCoffee(@AttrRef("typeOfCoffee") String typeOfCoffee) {
		this(null, typeOfCoffee);
	}

	public String getId() {
		return id;
	}

	public String getTypeOfCoffee() {
		return typeOfCoffee;
	}

	public void setTypeOfCoffee(String typeOfCoffee) {
		this.typeOfCoffee = typeOfCoffee;
	}

	public CoffeesByTypeReport getRptCoffeeByType() {
		return rptCoffeeByType;
	}

	@Override
	public String toString() {
		return "TypeOfCoffee (" + id + "," + typeOfCoffee + " )";
	}

	// automatically generate the next student id
	 private String nextID(String id) throws ConstraintViolationException {
		    if (id == null) { // generate a new id
		        idCounter++;
		      return "T" + idCounter;
		    } else {
		      // update id
		      int num;
		      try {
		        num = Integer.parseInt(id.substring(1));
		      } catch (RuntimeException e) {
		        throw new ConstraintViolationException(
		            ConstraintViolationException.Code.INVALID_VALUE, e, new Object[] { id });
		      }
		      
		      if (num > idCounter) {
		        idCounter = num;
		      }
		      
		      return id;
		    }
		  }

		  /**
		   * @requires 
		   *  minVal != null /\ maxVal != null
		   * @effects 
		   *  update the auto-generated value of attribute <tt>attrib</tt>, specified for <tt>derivingValue</tt>, using <tt>minVal, maxVal</tt>
		   */
		  @DOpt(type=DOpt.Type.AutoAttributeValueSynchroniser)
		  public static void updateAutoGeneratedValue(
		      DAttr attrib,
		      Tuple derivingValue, 
		      Object minVal, 
		      Object maxVal) throws ConstraintViolationException {
		    
		    if (minVal != null && maxVal != null) {
		      //TODO: update this for the correct attribute if there are more than one auto attributes of this class 

		      String maxId = (String) maxVal;
		      
		      try {
		        int maxIdNum = Integer.parseInt(maxId.substring(1));
		        
		        if (maxIdNum > idCounter) // extra check
		          idCounter = maxIdNum;
		        
		      } catch (RuntimeException e) {
		        throw new ConstraintViolationException(
		            ConstraintViolationException.Code.INVALID_VALUE, e, new Object[] {maxId});
		      }
		    }
		  }
}
