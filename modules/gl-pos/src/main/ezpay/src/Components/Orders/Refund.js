import React, { useState } from "react";
import styled from "styled-components";

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

const Refund = ({ ticket }) => {
  const [refunds, setRefunds] = useState(
    ticket.saleOrderLineList.map(() => ({ selected: false, quantity: 0 }))
  );

  const handleSelectChange = (index) => {
    const newRefunds = [...refunds];
    newRefunds[index].selected = !newRefunds[index].selected;
    setRefunds(newRefunds);
  };
  const getTotalQuantity = (itemId) => {
    console.log("Calculating total quantity for item id:", itemId);
    return ticket.saleOrderLineList.reduce((total, item) => {
      if (item.id === itemId) {
        return total + item.quantity;
      }
      return total;
    }, 0);
  };
  // const handleQuantityChange = (index, quantity) => {
  //   const newRefunds = [...refunds];
  //   newRefunds[index].quantity = Math.max(
  //     0,
  //     Math.min(ticket.saleOrderLineList[index].quantity, quantity)
  //   ); // Ensure quantity is between 0 and the max purchased quantity
  //   setRefunds(newRefunds);
  //   console.log('here')
  // };
  const handleQuantityChange = (index, quantity) => {
    const newRefunds = [...refunds];
    const itemId = newRefunds[index].id; // Assuming you have an id field to identify items
    const maxQuantity = getTotalQuantity(itemId); // Get the summed quantity
    newRefunds[index].quantity = Math.max(
      0,
      Math.min(maxQuantity, quantity)
    ); // Ensure quantity is between 0 and the max summed quantity
    setRefunds(newRefunds);
  };

  const totalRefund = refunds.reduce(
    (total, refund, index) =>
      total +
      (refund.selected
        ? refund.quantity * ticket.saleOrderLineList[index].price
        : 0),
    0
  );

  return (
    <>
      <Header>
        <Title>Refund Ticket N° {ticket.saleOrderSeq}</Title>
        <DateTime>{new Date(ticket.creationDate).toLocaleString()}</DateTime>
      </Header>
      <Section>
        <SubTitle>Purchased Items</SubTitle>
        <Table>
          <TableRow>
            <TableCell className="header">Select</TableCell>
            <TableCell className="header">Item</TableCell>
            <TableCell className="header alignRight">Price</TableCell>
            <TableCell className="header alignRight">
              Purchased Quantity
            </TableCell>
            <TableCell className="header alignRight">Refund Quantity</TableCell>
            <TableCell className="header alignRight">Total Refund</TableCell>
          </TableRow>
          {ticket.saleOrderLineList.map((item, index) => (
            <TableRow key={index}>
              <TableCell>
                <input
                  type="checkbox"
                  checked={refunds[index].selected}
                  onChange={() => handleSelectChange(index)}
                />
              </TableCell>
              <TableCell>{item.name}</TableCell>
              <TableCell className="alignRight">{item.price}DT</TableCell>
              <TableCell className="alignRight">{item.quantity}</TableCell>
              <TableCell className="alignRight">
                <Input
                  type="number"
                  value={refunds[index].quantity}
                  onChange={(e) =>
                    handleQuantityChange(index, parseInt(e.target.value, 10))
                  }
                  min="0"
                  max={item.quantity}
                />
              </TableCell>
              <TableCell className="alignRight">
                {(refunds[index].selected
                  ? refunds[index].quantity * item.price
                  : 0
                ).toFixed(2)}
                DT
              </TableCell>
            </TableRow>
          ))}
        </Table>
      </Section>
      <Section>
        <Total>
          <span>Total Refund</span>
          <span>{totalRefund.toFixed(3)}DT</span>
        </Total>
      </Section>
      <Section>
        <SubTitle>Customer Information</SubTitle>
        <Customer>
          {ticket.clientPartner ? ticket.clientPartner.simpleFullName : "No customer"}
        </Customer>
      </Section>
    </>
  );
};

export default Refund;
