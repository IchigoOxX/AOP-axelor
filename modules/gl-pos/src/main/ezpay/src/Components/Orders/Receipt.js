import React from 'react';
import Barcode from 'react-barcode';
import { useSelector } from 'react-redux';
import styled from 'styled-components';

const Container = styled.div`
  display: grid;
  width: 240px;
  padding: 10px;
  margin: 10px;
  font-family: Arial, sans-serif;
  border: solid black 1px;
`;
const Header = styled.div``;
const CompanyInfo = styled.div`
  text-align: center;
  margin-bottom: 10px;
`;
const CompanyLogo = styled.div`
  font-size: 30px;
`;
const CompanyAddress = styled.div`
  font-size: 12px;
`;
const TicketInfo = styled.div``;

const Title = styled.div``;

const DateTime = styled.div``;

const HashedLine = styled.div`
  border-top: dashed black 1px;
  margin: 10px 2px;
`;

const Item = styled.div`
  display: grid;
  grid-template-columns: 10fr 4fr 2.5fr 4fr;
  column-gap: 2px;
  font-size: 12px;
`;
const DetailsTitle = styled.div`
  justify-self: center;
`;
const Details = styled.div`
  justify-self: right;
`;
const TotalDetails = styled.div`
  display: flex;
  justify-content: space-between;
`;
const BarcodeDiv = styled(Barcode)`
  margin: auto;
`;

const Receipt = () => {
    const ticket = useSelector(state => state.cart.currentTicket);
  console.log('ticket: ', ticket);

    // const soldItems = ticket?.saleOrderLineList.filter(item => item.status === 'sold');
    const soldItems = ticket?.saleOrderLineList || [];

  return (
    <Container>
      <Header>
        <CompanyInfo>
          <CompanyLogo>Company Logo</CompanyLogo>
          <CompanyAddress>Address: Route something, sousse, tunisie.</CompanyAddress>
        </CompanyInfo>
        <TicketInfo>
          <Title>Receipt N° {ticket?.saleOrderSeq}</Title>
          <DateTime>Date: {new Date().toLocaleString()}</DateTime>
        </TicketInfo>
      </Header>
      <HashedLine />
      <Item>
        <div>ITEM</div>
        <DetailsTitle>U.P</DetailsTitle>
        <DetailsTitle>QTE</DetailsTitle>
        <DetailsTitle>TOTAL</DetailsTitle>
      </Item>
      {soldItems.map((item, index) => (
        <Item key={index}>
          <div>{item.productName}</div>
          <Details>{parseFloat(item.product.salePrice).toFixed(3)}</Details>
          <Details>x{parseInt(item.qty)}</Details>
          <Details>{(parseFloat(item.product.salePrice) * parseInt(item.qty)).toFixed(3)}</Details>
        </Item>
      ))}
      <HashedLine />
      <div>
        <TotalDetails>
          <div>Items Count</div>
          {/*<div>{soldItems.length}</div>*/}
            <div>{soldItems.reduce((totalQty, item) => totalQty + parseInt(item.qty), 0)}</div>
        </TotalDetails>
        <TotalDetails>
          <div>Total</div>
          <div>{soldItems.reduce((total, item) => total + parseFloat(item.product.salePrice) * parseInt(item.qty), 0).toFixed(3)}</div>
        </TotalDetails>
      </div>
      <HashedLine />
      <BarcodeDiv value="Barcode" width={1} height={22} font="20" />
    </Container>
  );
};

export default Receipt;
