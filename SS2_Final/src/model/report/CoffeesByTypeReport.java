package model.report;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import domainapp.basics.core.dodm.dsm.DSMBasic;
import domainapp.basics.core.dodm.qrm.QRM;
import domainapp.basics.exceptions.DataSourceException;
import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.model.Oid;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DClass;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.MetaConstants;
import domainapp.basics.model.meta.Select;
import domainapp.basics.model.query.Expression.Op;
import domainapp.basics.model.query.Query;
import domainapp.basics.model.query.QueryToolKit;
import domainapp.basics.modules.report.model.meta.Output;
import model.Coffee;
import model.TypeOfCoffee;

/**
 * @overview 
 * 	Represent the reports about students by name.
 * 
 * @author dmle
 *
 * @version 5.0
 */
@DClass(schema="SS2_Final",serialisable=false)
public class CoffeesByTypeReport {
	 @DAttr(name = "id", id = true, auto = true, type = Type.Integer, length = 5, optional = false, mutable = false)
	  private int id;
	  private static int idCounter = 0;

	  /**input: student name */
	  @DAttr(name = "type", type = Type.String, length = 10, optional = false)
	  private String type;
	  
	  /**output: students whose names match {@link #name} */
	  @DAttr(name="coffees",type=Type.Collection,optional=false, mutable=false,
	      serialisable=false,filter=@Select(clazz=Coffee.class, 
	      attributes={Coffee.C_id, Coffee.C_name, Coffee.C_type, Coffee.C_rptCoffeeByType, TypeOfCoffee.T_rptCoffeeByType})
	      ,derivedFrom={"type"}
	      )
	  @DAssoc(ascName="coffees-by-type-report-has-coffees",role="report",
	      ascType=AssocType.One2Many,endType=AssocEndType.One,
	    associate=@Associate(type=Coffee.class,cardMin=0,cardMax=MetaConstants.CARD_MORE
	    ))
	  @Output
	  private Collection<Coffee> coffees;

	  /**output: number of students found (if any), derived from {@link #students} */
	  @DAttr(name = "numCoffees", type = Type.Integer, length = 10, auto=true, mutable=false)
	  @Output
	  private int numCoffees;
	  
	  /**
	   * @effects 
	   *  initialise this with <tt>name</tt> and use {@link QRM} to retrieve from data source 
	   *  all {@link Student} whose names match <tt>name</tt>.
	   *  initialise {@link #students} with the result if any.
	   *  
	   *  <p>throws NotPossibleException if failed to generate data source query; 
	   *  DataSourceException if fails to read from the data source
	   * 
	   */
	  @DOpt(type=DOpt.Type.ObjectFormConstructor)
	  @DOpt(type=DOpt.Type.RequiredConstructor)
	  public CoffeesByTypeReport(@AttrRef("type") String type) throws NotPossibleException, DataSourceException {
	    this.id=++idCounter;
	    
	    this.type = type;
	    
	    doReportQuery();
	  }
	  
	  /**
	   * @effects return name
	   */
	  public String getType() {
	    return type;
	  }

	  /**
	   * @effects <pre>
	   *  set this.name = name
	   *  if name is changed
	   *    invoke {@link #doReportQuery()} to update the output attribute value
	   *    throws NotPossibleException if failed to generate data source query; 
	   *    DataSourceException if fails to read from the data source.
	   *  </pre>
	   */
	  public void setType(String type) throws NotPossibleException, DataSourceException {
//	    boolean doReportQuery = (name != null && !name.equals(this.name));
	    
	    this.type = type;
	    
	    // DONOT invoke this here if there are > 1 input attributes!
	    doReportQuery();
	  }

	  @DOpt(type = DOpt.Type.DerivedAttributeUpdater)
		@AttrRef(value = "coffees")
		public void doReportQuery() throws NotPossibleException, DataSourceException {
			// the query manager instance

			QRM qrm = QRM.getInstance();

			// create a query to look up Student from the data source
			// and then populate the output attribute (students) with the result
			DSMBasic dsm = qrm.getDsm();

			// TODO: to conserve memory cache the query and only change the query parameter
			// value(s)
			// look up TypeOfCustomer t such that name match type
			Query q1 = QueryToolKit.createSearchQuery(dsm, TypeOfCoffee.class, new String[] { TypeOfCoffee.T_name },
					new Op[] { Op.MATCH }, new Object[] { "%" + type + "%" });
			Map<Oid, TypeOfCoffee> result1 = qrm.getDom().retrieveObjects(TypeOfCoffee.class, q1);

			TypeOfCoffee t = null;
			if (result1 != null) {
				for (Entry<Oid, TypeOfCoffee> e : result1.entrySet()) {
					t = e.getValue();
					break;
				}
			}

			// if (t == null) throws exception

			// look up Customers such that typeOfCustomer Op.EQ= t
			Query q2 = QueryToolKit.createSearchQuery(dsm, Coffee.class, new String[] { Coffee.C_type },
					new Op[] { Op.EQ }, new Object[] { t });

			Map<Oid, Coffee> result2 = qrm.getDom().retrieveObjects(Coffee.class, q2);

			if (result2 != null) {
				// update the main output data
				coffees = result2.values();

				// update other output (if any)
				numCoffees = coffees.size();
			} else {
				// no data found: reset output
				resetOutput();
			}
		}

	  /**
	   * @effects 
	   *  reset all output attributes to their initial values
	   */
	  private void resetOutput() {
	    coffees = null;
	    numCoffees = 0;
	  }

	  /**
	   * A link-adder method for {@link #students}, required for the object form to function.
	   * However, this method is empty because students have already be recorded in the attribute {@link #students}.
	   */
	  @DOpt(type=DOpt.Type.LinkAdder)
	  public boolean addCoffee(Collection<Coffee> coffees) {
	    // do nothing
	    return false;
	  }
	  
	  /**
	   * @effects return students
	   */
	  public Collection<Coffee> getCoffees() {
	    return coffees;
	  }
	  
	  /**
	   * @effects return numStudents
	   */
	  public int getNumCoffees() {
	    return numCoffees;
	  }

	  /**
	   * @effects return id
	   */
	  public int getId() {
	    return id;
	  }

	  /* (non-Javadoc)
	   * @see java.lang.Object#hashCode()
	   */
	  /**
	   * @effects 
	   * 
	   * @version 
	   */
	  @Override
	  public int hashCode() {
	    final int prime = 31;
	    int result = 1;
	    result = prime * result + id;
	    return result;
	  }

	  /* (non-Javadoc)
	   * @see java.lang.Object#equals(java.lang.Object)
	   */
	  /**
	   * @effects 
	   * 
	   * @version 
	   */
	  @Override
	  public boolean equals(Object obj) {
	    if (this == obj)
	      return true;
	    if (obj == null)
	      return false;
	    if (getClass() != obj.getClass())
	      return false;
	    CoffeesByTypeReport other = (CoffeesByTypeReport) obj;
	    if (id != other.id)
	      return false;
	    return true;
	  }

	  /* (non-Javadoc)
	   * @see java.lang.Object#toString()
	   */
	  /**
	   * @effects 
	   * 
	   * @version 
	   */
	  @Override
	  public String toString() {
	    return "CoffeesByTypeReport (" + id + ", " + type + ")";
	  }

}
