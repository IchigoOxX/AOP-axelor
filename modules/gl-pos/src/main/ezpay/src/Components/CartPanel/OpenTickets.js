import React, { useState } from 'react';
import Popup from '../Common/Popup';
import Ticket from './Ticket';
import styled from 'styled-components';
import { useSelector } from 'react-redux';

const Container = styled.div`
  display: flex;
  height: calc(100vh - 180px);
`;

const FilterList = styled.ul`
  display: flex;
  flex-direction: column;
  width: 200px;
  list-style: none;
  margin: 0;
  padding: 0;
  background-color: #f1f7fb;
  border-right: 1px solid lightgrey;
`;

const FilterItem = styled.li`
  padding: 15px 20px;
  cursor: pointer;
  background-color: ${(props) => (props.active ? '#0c085c' : 'transparent')};
  color: ${(props) => (props.active ? 'white' : '#0c085c')};
  border-left: ${(props) => (props.active ? '5px solid #0c085c' : 'none')};
  transition: background-color 0.3s ease, border-left 0.3s ease;
  &:hover {
    background-color: ${(props) => (props.active ? '#0a074a' : '#ececec')};
  }
`;

const Tickets = styled.div`
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  padding: 15px;
  background-color: #fff;
  overflow-y: auto;
`;

const OpenTickets = (props) => {
    const [selectedFilter, setSelectedFilter] = useState('all');
    const tickets = useSelector((state) =>
        state.tickets.tickets
            .filter((ticket) => {
                if (selectedFilter === 'drafts') {
                    return ticket.statusSelect === 1;
                }
                if (selectedFilter === 'confirmed') {
                    return ticket.statusSelect === 3;
                }
                return true;
            })
            .sort((a, b) => {
                const dateA = new Date(a.updatedOn || a.createdOn);
                const dateB = new Date(b.updatedOn || b.createdOn);
                return dateB - dateA;
            })
    );


    return (
        <Popup title="Open Tickets" onClose={props.closeOpenTickets}>
            <Container>
                <FilterList>
                    <FilterItem
                        active={selectedFilter === 'all'}
                        onClick={() => setSelectedFilter('all')}
                    >
                        All
                    </FilterItem>
                    <FilterItem
                        active={selectedFilter === 'drafts'}
                        onClick={() => setSelectedFilter('drafts')}
                    >
                        Drafts
                    </FilterItem>
                    <FilterItem
                        active={selectedFilter === 'confirmed'}
                        onClick={() => setSelectedFilter('confirmed')}
                    >
                        Confirmed
                    </FilterItem>
                </FilterList>
                <Tickets>
                    {tickets.map((ticket) => (
                        <Ticket
                            key={ticket.id}
                            ticket={ticket}
                            openTicketDetails={props.openTicketDetails}
                            onClose={props.closeOpenTickets}
                        />
                    ))}
                </Tickets>
            </Container>
        </Popup>
    );
};

export default OpenTickets;
