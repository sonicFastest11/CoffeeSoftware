package model.report;

import java.util.Collection;
import java.util.Map;

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
import model.SaleOrder;

/**
 * @overview 
 * 	Represent the reports about students by name.
 * 
 * @author dmle
 *
 * @version 5.0
 */
@DClass(schema="SS2_Final",serialisable=false)
public class SaleOrdersByDateReport {
  @DAttr(name = "id", id = true, auto = true, type = Type.Integer, length = 5, optional = false, mutable = false)
  private int id;
  private static int idCounter = 0;

  /**input: student name */
  @DAttr(name = "date", type = Type.String, length = 15, optional = false)
  private String date;
  
  /**output: students whose names match {@link #name} */
  @DAttr(name="saleOrders",type=Type.Collection,optional=false, mutable=false,
      serialisable=false,filter=@Select(clazz=SaleOrder.class, 
      attributes={SaleOrder.SO_id, SaleOrder.SO_cus, SaleOrder.SO_sell, SaleOrder.SO_date, 
    		  SaleOrder.SO_rptSaleOrderByDate})
      ,derivedFrom={"date"}
      )
  @DAssoc(ascName="saleOrders-by-date-report-has-saleOrders",role="report",
      ascType=AssocType.One2Many,endType=AssocEndType.One,
    associate=@Associate(type=SaleOrder.class,cardMin=0,cardMax=MetaConstants.CARD_MORE
    ))
  @Output
  private Collection<SaleOrder> saleOrders;

  /**output: number of students found (if any), derived from {@link #students} */
  @DAttr(name = "numSaleOrders", type = Type.Integer, length = 10, auto=true, mutable=false)
  @Output
  private int numSaleOrders;
  
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
  public SaleOrdersByDateReport(@AttrRef("date") String date) throws NotPossibleException, DataSourceException {
    this.id=++idCounter;
    
    this.date = date;
    
    doReportQuery();
  }
  
  /**
   * @effects return name
   */
  public String getDate() {
    return date;
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
  public void setDate(String date) throws NotPossibleException, DataSourceException {
//    boolean doReportQuery = (name != null && !name.equals(this.name));
    
    this.date = date;
    
    // DONOT invoke this here if there are > 1 input attributes!
    doReportQuery();
  }

  /**
   * This method is invoked when the report input has be set by the user. 
   * 
   * @effects <pre>
   *   formulate the object query
   *   execute the query to retrieve from the data source the domain objects that satisfy it 
   *   update the output attributes accordingly.
   *  
   *  <p>throws NotPossibleException if failed to generate data source query; 
   *  DataSourceException if fails to read from the data source. </pre>
   */
  @DOpt(type=DOpt.Type.DerivedAttributeUpdater)
  @AttrRef(value="saleOrders")
  public void doReportQuery() throws NotPossibleException, DataSourceException {
    // the query manager instance
    
    QRM qrm = QRM.getInstance();
    
    // create a query to look up Student from the data source
    // and then populate the output attribute (students) with the result
    DSMBasic dsm = qrm.getDsm();
    
    //TODO: to conserve memory cache the query and only change the query parameter value(s)
    Query q = QueryToolKit.createSearchQuery(dsm, SaleOrder.class, 
        new String[] {SaleOrder.SO_date}, 
        new Op[] {Op.MATCH}, 
        new Object[] {"%"+date+"%"});
    
    Map<Oid, SaleOrder> result = qrm.getDom().retrieveObjects(SaleOrder.class, q);
    
    if (result != null) {
      // update the main output data 
      saleOrders = result.values();
      
      // update other output (if any)
      numSaleOrders = saleOrders.size();
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
    saleOrders = null;
    numSaleOrders = 0;
  }

  /**
   * A link-adder method for {@link #students}, required for the object form to function.
   * However, this method is empty because students have already be recorded in the attribute {@link #students}.
   */
  @DOpt(type=DOpt.Type.LinkAdder)
  public boolean addSaleOrder(Collection<SaleOrder> saleOrders) {
    // do nothing
    return false;
  }
  
  /**
   * @effects return students
   */
  public Collection<SaleOrder> getSaleOrders() {
    return saleOrders;
  }
  
  /**
   * @effects return numStudents
   */
  public int getNumSaleOrders() {
    return numSaleOrders;
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
    SaleOrdersByDateReport other = (SaleOrdersByDateReport) obj;
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
    return "SaleOrdersByDateReport (" + id + ", " + date + ")";
  }

}
