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
import model.report.SaleOrdersByDateReport;

public class SaleOrder {
	public static final String SO_id = "id";
	public static final String SO_cus = "customer";
	public static final String SO_sell = "seller";
	public static final String SO_date = "date";
	public static final String SO_rptSaleOrderByDate = "rptSaleOrderByDate";

	private static int idCounter = 0;

	@DAttr(name = SO_id, id = true, auto = true, type = Type.String, length = 6, mutable = false, optional = false)
	private String id;

	@DAttr(name = SO_cus, type = Type.Domain, length = 30, optional = false)
	private Customer customer;

	@DAttr(name = SO_sell, type = Type.Domain, length = 30, optional = false)
	private Seller seller;

	@DAttr(name = SO_date, type = Type.String, length = 10, optional = false)
	private String date;

	@DAttr(name = SO_rptSaleOrderByDate, type = Type.Domain, serialisable = false,
			// IMPORTANT: set virtual=true to exclude this attribute from the object state
			// (avoiding the view having to load this attribute's value from data source)
			virtual = true)
	private SaleOrdersByDateReport rptSaleOrderByDate;

	// @DAttr(name = "quantity", type = Type.Integer, length = 30, optional = false)
	// private Integer quantity;
	//
	// @DAttr(name = "totalPrice", type = Type.Integer, auto = true, mutable =
	// false, optional = true, serialisable = false, derivedFrom = {
	// "unitPrice", "quantity" })
	// private Integer totalPrice;

	@DAttr(name = "detailExOrders", type = Type.Collection, optional = false, serialisable = false, filter = @Select(clazz = DetailExOrder.class))
	@DAssoc(ascName = "saleOrder-has-detailExOrders", role = "saleOrder", ascType = AssocType.One2Many, endType = AssocEndType.One, associate = @Associate(type = DetailExOrder.class, cardMin = 0, cardMax = 30))
	private Collection<DetailExOrder> detailExOrders;

	private int count;
	// private double totalPrice;
	@DAttr(name = "totalPrice", type = Type.Double, auto = true, mutable = false, optional = true, serialisable = true)
	private Double totalPrice;

	@DOpt(type = DOpt.Type.DataSourceConstructor)
	public SaleOrder(String id, Customer customer, Seller seller, String date, Double totalPrice) {
		this.id = nextID(id);
		this.customer = customer;
		this.seller = seller;
		this.date = date;
		if (totalPrice == null)
			this.totalPrice = 0d;
		else
			this.totalPrice = totalPrice;

		detailExOrders = new ArrayList<>();
		// calTotal();
		count = 0;
		// total=0;
	}

	// public void takeDate() {
	// Date date1 = new Date();
	// SimpleDateFormat ft = new SimpleDateFormat("yyyy/MM/dd ");
	// date = ft.format(date1);
	// }

	@DOpt(type = DOpt.Type.ObjectFormConstructor)
	@DOpt(type = DOpt.Type.RequiredConstructor)
	public SaleOrder(@AttrRef("customer") Customer customer, @AttrRef("seller") Seller seller,
			@AttrRef("date") String date) {
		this(null, customer, seller, date, null);
	}

	public Customer getCustomer() {
		return customer;
	}

	public void setCustomer(Customer customer) {
		this.customer = customer;
	}

	public Seller getSeller() {
		return seller;
	}

	public void setSeller(Seller seller) {
		this.seller = seller;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getId() {
		return id;
	}

	public SaleOrdersByDateReport getRptSaleOrderByDate() {
		return rptSaleOrderByDate;
	}

	@DOpt(type = DOpt.Type.LinkAdder)
	public boolean addDetailExOrder(DetailExOrder d) {
		if (!detailExOrders.contains(d))
			detailExOrders.add(d);

		return false;
	}

	@DOpt(type = DOpt.Type.LinkAdderNew)
	public boolean addNewDetailExOrder(DetailExOrder d) {
		detailExOrders.add(d);

		count++;
		computeTotalPrice();
		return true;
	}

	@DOpt(type = DOpt.Type.LinkAdder)
	// @MemberRef(name="enrolments")
	public boolean addDetailExOrder(Collection<DetailExOrder> deos) {
		boolean added = false;
		for (DetailExOrder d : deos) {
			if (!detailExOrders.contains(d)) {
				if (!added)
					added = true;
				detailExOrders.add(d);
			}
		}
		return false;
	}

	@DOpt(type = DOpt.Type.LinkAdderNew)
	public boolean addNewDetailExOrder(Collection<DetailExOrder> deos) {
		detailExOrders.addAll(deos);
		count += deos.size();
		computeTotalPrice();
		return true;
	}

	@DOpt(type = DOpt.Type.LinkRemover)
	public boolean removeDetailExOrder(DetailExOrder d) {
		boolean removed = detailExOrders.remove(d);

		if (removed) {
			count--;
			computeTotalPrice();
			return true;
		}
		return false;
	}

	@DOpt(type = DOpt.Type.LinkUpdater)
	// @MemberRef(name="enrolments")
	public boolean updateDetailExOrder(DetailExOrder d) throws IllegalStateException {
		// recompute using just the affected enrolment
		double total = totalPrice;

		double oldtotal = d.getTotalPrice(true);

		double diff = d.getTotalPrice() + oldtotal;

		// TODO: cache totalMark if needed

		total += diff;

		totalPrice = total;

		// no other attributes changed
		return true;
	}

	public void setDetailExOrder(Collection<DetailExOrder> deo) {
		this.detailExOrders = deo;
		count = deo.size();
		computeTotalPrice();
	}

	private void computeTotalPrice() {
		// if (count > 0) {
		double total = 0d;
		for (DetailExOrder e : detailExOrders) {
			total += e.getTotalPrice();
		}

		totalPrice = total;
		// } else {
		// totalPrice = 0d;
		// }
	}

	// v2.6.4.b
	public double getTotalPrice() {
		return totalPrice;
	}

	public Collection<DetailExOrder> getDetailExOrders() {
		return detailExOrders;
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
		return "SaleOrder(" + id + "," + customer + "," + seller + "," + date + ")";
	}

	public String nextID(String id) throws ConstraintViolationException {
		if (id == null) { // generate a new id
			idCounter++;

			return "SO" + idCounter;
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
