package model;

import java.util.ArrayList;
import java.util.Collection;

import domainapp.basics.exceptions.ConstraintViolationException;
import domainapp.basics.model.meta.AttrRef;
import domainapp.basics.model.meta.DAssoc;
import domainapp.basics.model.meta.DAssoc.AssocEndType;
import domainapp.basics.model.meta.DAssoc.AssocType;
import domainapp.basics.model.meta.DAssoc.Associate;
import domainapp.basics.model.meta.DAttr;
import domainapp.basics.model.meta.DAttr.Type;
import domainapp.basics.model.meta.DOpt;
import domainapp.basics.model.meta.Select;
import domainapp.basics.util.Tuple;
import model.report.ImportOrdersByDateReport;

public class ImportOrder {
	
	public static final String IO_id = "id";
	public static final String IO_sup = "supplier";
	public static final String IO_im = "importer";
	public static final String IO_date = "date";
	public static final String IO_rptImportOrderByDate = "rptImportOrdersByDate";

	@DAttr(name = IO_id, id = true, auto = true, type = Type.String, length = 6, mutable = false, optional = false)
	private String id;


	@DAttr(name = IO_sup, type = Type.Domain, optional = false, length = 6)
	@DAssoc(ascName = "importOrder-has-supplier", ascType = AssocType.One2Many, endType = AssocEndType.Many, role = "importOrder", associate = @Associate(cardMax = 25, cardMin = 1, type = Supplier.class))
	private Supplier supplier;
	
	@DAttr(name = IO_im, type = Type.Domain, optional = false, length = 6)
	@DAssoc(ascName = "importOrder-has-importer", ascType = AssocType.One2Many, endType = AssocEndType.Many, role = "importOrder", associate = @Associate(cardMax = 25, cardMin = 1, type = Importer.class))
	private Importer importer;


	@DAttr(name = IO_date, type = Type.String,length = 10, optional = false)
	private String date;
	
	@DAttr(name = IO_rptImportOrderByDate, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private ImportOrdersByDateReport rptImportOrdersByDate;
	private static int idCounter = 0;
	
	@DAttr(name = "detailImOrders", type = Type.Collection, optional = false, 
			serialisable = false, filter = @Select(clazz = DetailImOrder.class))
	@DAssoc(ascName = "importOrder-has-detailImOrders", role = "importOrder", 
	ascType = AssocType.One2Many, endType = AssocEndType.One, 
	associate = @Associate(type = DetailImOrder.class, cardMin = 0, cardMax = 30))
	private Collection<DetailImOrder> detailImOrders;

	private int count;
	
	@DAttr(name = "totalPrice", type = Type.Double, auto = true, mutable = false,
			 optional = true, serialisable = true)
			 private Double totalPrice;
	
	@DOpt(type=DOpt.Type.DataSourceConstructor)
	public ImportOrder(String id, Supplier supplier ,Importer importer, String date, Double totalPrice) {
		this.id = nextID(id);
		this.supplier = supplier;
		this.importer = importer;
		this.date=date;
		if (totalPrice == null)
			this.totalPrice = 0d;
		else
			this.totalPrice=totalPrice;
		
		detailImOrders = new ArrayList<>();
		count = 0;

	}
	
	@DOpt(type=DOpt.Type.ObjectFormConstructor)
	 @DOpt(type=DOpt.Type.RequiredConstructor)
	public ImportOrder(@AttrRef("supplier") Supplier supplier, @AttrRef("importer") Importer importer,
			@AttrRef("date") String date  ) {
		this(null, supplier, importer,date,null );
	}
//	public void takeDate() {
//		Date date1 = new Date();
//		SimpleDateFormat ft = 
//			      new SimpleDateFormat ("yyyy/MM/dd ");
//		date = ft.format(date1);
//	}
	
	public String getId() {
		return id;
	}


	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier supplier) {
		this.supplier = supplier;
	}

	public Importer getImporter() {
		return importer;
	}

	public void setImporter(Importer importer) {
		this.importer = importer;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}
	public ImportOrdersByDateReport getRptImportOrdersByDate() {
		return rptImportOrdersByDate;
	}

	@DOpt(type = DOpt.Type.LinkAdder)
	public boolean addDetailImOrder(DetailImOrder d) {
		if (!detailImOrders.contains(d))
			detailImOrders.add(d);

		return false;
	}

	@DOpt(type = DOpt.Type.LinkAdderNew)
	public boolean addNewDetailImOrder(DetailImOrder d) {
		detailImOrders.add(d);

		count++;
		computeTotalPrice();
		return true;
	}

	@DOpt(type = DOpt.Type.LinkAdder)
	public boolean addDetailImOrder(Collection<DetailImOrder> dios) {
		boolean added = false;
		for (DetailImOrder d : dios) {
			if (!detailImOrders.contains(d)) {
				if (!added)
					added = true;
				detailImOrders.add(d);
			}
		}
		return false;
	}

	@DOpt(type = DOpt.Type.LinkAdderNew)
	public boolean addNewDetailImOrder(Collection<DetailImOrder> dios) {
		detailImOrders.addAll(dios);
		count += dios.size();
		computeTotalPrice();
		return true;
	}

	@DOpt(type = DOpt.Type.LinkRemover)
	public boolean removeDetailImOrder(DetailImOrder d) {
		boolean removed = detailImOrders.remove(d);

		if (removed) {
			count--;
			computeTotalPrice();
			return true;
		} else {
			return false;
		}
	}
	
	@DOpt(type = DOpt.Type.LinkUpdater)
	// @MemberRef(name="enrolments")
	public boolean updateDetailImOrder(DetailExOrder d) throws IllegalStateException {
		// recompute using just the affected enrolment
		double total = totalPrice ;

		double oldtotal = d.getTotalPrice(true);

		double diff = d.getTotalPrice() + oldtotal;

		// TODO: cache totalMark if needed

		total += diff;

		totalPrice = total ;

		// no other attributes changed
		return true;
	}

	public void setDetailImOrder(Collection<DetailImOrder> dio) {
		this.detailImOrders = dio;
		count = dio.size();

	}

	private void computeTotalPrice() {
		//if (count > 0) {
			double total = 0d;
			for (DetailImOrder e : detailImOrders) {
				total += e.getTotalPrice();
			}

			totalPrice = total ;
		//} else {
		//	totalPrice = 0d;
		//}
	}
	

//	public void setTotalPrice(double totalPrice) {
//		this.totalPrice = totalPrice;
//	}

	// v2.6.4.b
	public double getTotalPrice() {
		return totalPrice;
	}
	
	public Collection<DetailImOrder> getDetailImOrders() {
		return detailImOrders;
	}

	@DOpt(type = DOpt.Type.LinkCountGetter)
	public int getCount() {
		return count;
		// return enrolments.size();
	}

	@DOpt(type = DOpt.Type.LinkCountSetter)
	public void setCount(int count1) {
		count = count1;
	}
	
	@Override
	public String toString() {
		return "Order(" + id + "," + supplier + "," + importer + "," + date + ")";
	}

	public String nextID(String id) throws ConstraintViolationException {
		if (id == null) { // generate a new id
			idCounter++;

			return "IO" + idCounter;
		} else {
			// update id
			int num;
			try {
				num = Integer.parseInt(id.substring(2));
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
		
		if (attrib.name().equals("id")) {
			if (minVal != null && maxVal != null) {
				// TODO: update this for the correct attribute if there are more than one auto
				// attributes of this class
	
				String maxId = (String) maxVal;
	
				try {
					int maxIdNum = Integer.parseInt(maxId.substring(2));
	
					if (maxIdNum > idCounter) // extra check
						idCounter = maxIdNum;
	
				} catch (RuntimeException e) {
					throw new ConstraintViolationException(ConstraintViolationException.Code.INVALID_VALUE, e,
							new Object[] { maxId });
				}
			}
		}
	}

}
