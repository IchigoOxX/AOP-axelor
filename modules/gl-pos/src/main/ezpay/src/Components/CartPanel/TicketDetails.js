import React from 'react';
import Popup from '../Common/Popup';
import OrderContent from '../Orders/OrderContent';

const TicketDetails = ({ saleOrderSeq, closeTicketDetails, openTicketsList }) => {
  const returnToTicketsList = event => {
    closeTicketDetails(event);
    openTicketsList(event);
  };

  return (
    <Popup title="Ticket Details" onClose={returnToTicketsList}>
      <OrderContent saleOrderSeq={saleOrderSeq} />
    </Popup>
  );
};

export default TicketDetails;
