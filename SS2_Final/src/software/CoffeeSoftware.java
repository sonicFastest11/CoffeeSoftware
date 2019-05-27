package software;


import domainapp.basics.exceptions.NotPossibleException;
import domainapp.basics.software.DomainAppToolSoftware;
import model.Address;
import model.Coffee;
import model.Customer;
import model.DetailExOrder;
import model.DetailImOrder;
import model.District;
import model.ImportOrder;
import model.Importer;
import model.SaleOrder;
import model.Seller;
import model.Street;
import model.Supplier;
import model.TypeOfCoffee;
import model.report.CoffeesByTypeReport;
import model.report.CustomersByNameReport;
import model.report.ImportOrdersByDateReport;
import model.report.SaleOrdersByDateReport;
import model.report.SuppliersByNameReport;



/**
 * @overview 
 *  Encapsulate the basic functions for setting up and running a software given its domain model.  
 *  
 * @author dmle
 *
 * @version 
 */
public class CoffeeSoftware extends DomainAppToolSoftware {
  
  // the domain model of software
  private static final Class[] model = {
	  Coffee.class,
	  Address.class,
	  Street.class,
	  District.class,
	  Importer.class,
	  ImportOrder.class,
	  Supplier.class,
	  Customer.class,
	  SaleOrder.class,
	  DetailExOrder.class,
	  Seller.class,
	  DetailImOrder.class,
	  TypeOfCoffee.class,
	  CustomersByNameReport.class,
	  CoffeesByTypeReport.class,
	  SuppliersByNameReport.class,
	  ImportOrdersByDateReport.class,
	  SaleOrdersByDateReport.class
      

      // reports
 
  };
  
  /* (non-Javadoc)
   * @see vn.com.courseman.software.Software#getModel()
   */
  /**
   * @effects 
   *  return {@link #model}.
   */
  @Override
  protected Class[] getModel() {
    return model;
  }

  /**
   * The main method
   * @effects 
   *  run software with a command specified in args[0] and with the model 
   *  specified by {@link #getModel()}. 
   *  
   *  <br>Throws NotPossibleException if failed for some reasons.
   */
  public static void main(String[] args) throws NotPossibleException {
    new CoffeeSoftware().exec(args);
  }
}

