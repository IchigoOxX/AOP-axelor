import React, { useEffect, useState } from "react";
import { useDispatch, useSelector } from "react-redux";
import styled from "styled-components";
import { refundItems, selectTicketByNumber } from "../Redux/ticketSlice";
import { useNavigate } from "react-router-dom";

const Header = styled.div`
  display: flex;
  justify-content: space-between;
  border-bottom: 2px solid #ccc;
  padding-bottom: 10px;
  margin-bottom: 20px;
  align-items: center;
  text-align: center;
`;

const Title = styled.h1`
  font-size: 24px;
  color: #0c085c;
`;

const DateTime = styled.p`
  font-size: 16px;
  color: #868686;
`;

const Section = styled.div`
  margin-bottom: 20px;
`;

const SubTitle = styled.h2`
  font-size: 18px;
  color: #0c085c;
  margin-bottom: 10px;
`;

const Table = styled.div`
  display: table;
  width: 100%;
  border-collapse: collapse;
`;

const TableRow = styled.div`
  display: table-row;
`;

const TableCell = styled.div`
  display: table-cell;
  padding: 15px;
  border-bottom: 1px solid #ddd;
  text-align: left;
  background-color: transparent;
  font-weight: normal;

  &.header {
    background-color: #f0f0f0;
    font-weight: bold;
  }

  &.alignRight {
    text-align: right;
  }
`;

const Total = styled.div`
  display: flex;
  justify-content: space-between;
  padding: 10px;
  background-color: #e0e0e0;
  border-radius: 5px;
  font-weight: bold;
`;

const Customer = styled.div`
  font-size: 16px;
  color: #0c085c;
`;

const Input = styled.input`
  width: 50px;
  padding: 5px;
  font-size: 14px;
  text-align: center;
`;

const ButtonContainer = styled.div`
  display: flex;
  justify-content: center;
`;

const Button = styled.button`
  padding: 10px 20px;
  font-size: 16px;
  color: #fff;
  background-color: #0c085c;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  margin-top: 20px;
  &:disabled {
    background-color: #ccc;
    cursor: not-allowed;
  }
`;

const OrderContent = ({ saleOrderSeq, mode }) => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const ticket = useSelector((state) =>
    selectTicketByNumber(state, saleOrderSeq)
  );
  const [refunds, setRefunds] = useState([]);

  useEffect(() => {
    if (ticket) {
      setRefunds(
        ticket.saleOrderLineList.map((item) => ({
          id: item?.id,
          selected: item?.status === "refund",
          qty: item?.status === "refund" ? item?.qty : 0,
        }))
      );
    }
  }, [ticket]);

  const handleSelectChange = (index) => {
    setRefunds((prevRefunds) =>
      prevRefunds.map((refund, i) =>
        i === index
          ? {
              ...refund,
              selected: !refund.selected,
              qty:
                !refund.selected ||
                ticket.saleOrderLineList[index]?.status === "refund"
                  ? ticket.saleOrderLineList[index]?.qty
                  : 0,
            }
          : refund
      )
    );
  };
  const getStatusLabel = (status) => {
    switch (status) {
      case 1:
        return 'Sold';
      case 2:
        return 'Refund';
      default:
        return '_';
    }
  };
  const getTotalQuantity = (itemId) => {
    return ticket.saleOrderLineList.reduce((total, item) => {
      if (item?.id === itemId) {
        return total + item?.qty;
      }
      return total;
    }, 0);
  };
  const handleQuantityChange = (index, qty) => {
    const newRefunds = [...refunds];
    const itemId = newRefunds[index]?.id; // Assuming you have an id field to identify items
    const maxQuantity = getTotalQuantity(itemId); // Get the summed quantity
    const validQuantity = Math.max(0, Math.min(maxQuantity, qty));
    setRefunds((prevRefunds) =>
      prevRefunds.map((refund, i) =>
        i === index ? { ...refund, qty: validQuantity } : refund
      )
    );
  };

  const totalRefund = refunds.reduce(
    (total, refund, index) =>
      total +
      (refund.selected
        ? refund.qty * ticket.saleOrderLineList[index]?.product.salePrice
        : 0),
    0
  );

  const handleConfirmRefund = () => {
    // const refundedItems = ticket.saleOrderLineList
    //   .map((item, index) => ({
    //     id: item?.id,
    //     qty: refunds[index].qty,
    //   }))
    //   .filter((refundItem) => refunditem?.qty > 0);
    const refundedItems = ticket.saleOrderLineList.map((item, index) => ({
      id: item?.id,
      qty: refunds[index]?.qty,
      selected: refunds[index]?.selected,
      status: ticket.saleOrderLineList[index]?.status,
    }));
    dispatch(
      refundItems({
        saleOrderSeq: ticket.saleOrderSeq,
        refundedItems,
      })
    );
    console.log("refundedItems: ", refundedItems);
    navigate("/orders");
  };

  return (
    <>
      <Header>
        <Title>Ticket N° {ticket.saleOrderSeq}</Title>
        <DateTime>{new Date(ticket.creationDate).toLocaleString()}</DateTime>
      </Header>
      <Section>
        <SubTitle>Purchased Items</SubTitle>
        <Table>
          <TableRow>
            {mode === "refund" && (
              <TableCell className="header">Select</TableCell>
            )}
            <TableCell className="header">Item</TableCell>
            <TableCell className="header alignRight">Price</TableCell>
            <TableCell className="header alignRight">Quantity</TableCell>
            {mode !== "refund" && ticket.statusSelect === 4 && (
              <TableCell className="header alignRight">Status</TableCell>
            )}
            {mode === "refund" && (
              <TableCell className="header alignRight">
                Refund Quantity
              </TableCell>
            )}
            {mode !== "refund" && (
              <TableCell className="header alignRight">Total</TableCell>
            )}
          </TableRow>
          {ticket.saleOrderLineList.map((item, index) => (
            <TableRow key={index}>
              {mode === "refund" && (
                <TableCell>
                  <Input
                    type="checkbox"
                    checked={refunds[index]?.selected || false}
                    onChange={() => handleSelectChange(index)}
                  />
                </TableCell>
              )}
              <TableCell>{item?.productName}</TableCell>
              <TableCell className="alignRight">{parseFloat(item?.product.salePrice).toFixed(3)}DT</TableCell>
              <TableCell className="alignRight">{parseInt(item?.qty)}</TableCell>
              {mode !== "refund" && ticket.statusSelect === 4 && (
                <TableCell className="alignRight">{getStatusLabel(item?.status)}</TableCell>
              )}
              {mode === "refund" && (
                <TableCell className="alignRight">
                  <Input
                    type="number"
                    value={refunds[index]?.qty || 0}
                    onChange={(e) =>
                      handleQuantityChange(index, parseInt(e.target.value, 10))
                    }
                    min="0"
                    max={getTotalQuantity(item?.id)}
                  />
                </TableCell>
              )}
              {mode !== "refund" && (
                <TableCell className="alignRight">
                  {(item?.product.salePrice * item?.qty).toFixed(3)}DT
                </TableCell>
              )}
            </TableRow>
          ))}
        </Table>
      </Section>
      <Section>
        <Total>
          <span>Total</span>
          <span>{parseFloat(ticket.amountInvoiced).toFixed(3) || 0}DT</span>
        </Total>
      </Section>
      {mode === "refund" && (
        <Section>
          <Total>
            <span>Total Refund</span>
            <span>{totalRefund.toFixed(3)}DT</span>
          </Total>
        </Section>
      )}
      {ticket.clientPartner && !ticket.clientPartner.fullName.includes("Unknown") && (
          <Section>
            <SubTitle>Customer Information</SubTitle>
              <Customer>
                {ticket.clientPartner.simpleFullName}
                {ticket.clientPartner.emailAddress ? ticket.clientPartner.emailAddress.address : ''}
                {ticket.clientPartner.mobilePhone ? ticket.clientPartner.mobilePhone : ticket.clientPartner.fixedPhone ? ticket.clientPartner.fixedPhone : ''}
              </Customer>
          </Section>
      )
      }
      {mode === "refund" && (
        <ButtonContainer>
          <Button onClick={handleConfirmRefund}>Save</Button>
        </ButtonContainer>
      )}
    </>
  );
};

export default OrderContent;
