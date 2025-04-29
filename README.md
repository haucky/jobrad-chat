# jobrad-chat

This document outlines some technical approaches for a Customer Support Chat Solution. I tried to cover this in different levels of complexity/maturity.

## Assumed Business Requirements & Cross-Functional Requirements
To begin with I started out brainstorming different features & assumptions for a customer service.

- Different user roles, customer and agent
- Simplification: It is assumed that all customers are authenticated (no help for guests)
- Simplification: One agent is assigned to all customers (later there could be some distribution system, the most simple would be Round-Robin)
- Customer should be able to create one or more tickets
- Agent/Customer should read each other's messages in realtime (latency < 1 sec)
- Customer/Agent should be able to access the chat history
- Agent should be able to resolve a ticket

More features (*Not implemented*)
- Customer should be able to rate the experience after ticket was closed
- Customer/Agent should see unread messages visually highlighted
- Customer should be able to upload media (e.g. photo of a damaged bike)
- Agent sees tickets with high priority first (some kind of queue management)
- Agent should see newly created tickets right away
- Customer/Agent see which party is currently online

## Proposed solution
### Level 0 (Quick-Win)
- Jobrad already has heavy dependencies on the Odoo ERP
- Often the best solution is to not reinvent the wheel
- Use an existing module for doing
  this: [livechat](https://www.odoo.com/documentation/18.0/applications/websites/livechat.html)

### Level 1: Build your own REST service
- A first approach would be to build a simple REST API service
- Entities like User, Ticket and Messages are persisted in a relational database
- State is managed via simple CRUD operations
- For approximating realtime messaging and realtime dashboards would be accomplished via (long)polling
- Frontend: For pure server-side rendering the page would reload quite a bit, since a lot of DOM updates are required when sending and receiving messages.

### Level 2: SSE + REST API (implemented)
- For realtime updates on new messages SSE is used as communication protocol
- Since SSE serves only in one direction (server-to-client), client to server communication is still done via REST operations
- Frontend: Server-Side rendering with `Thymeleaf` and `htmx` for dynamic DOM updates
- Backend: Java Spring Boot
- Very simple domain model, with instances only kept in memory (no persistence layer)
  - Simplification: Services partially act as in-memory repositories
  - Simplification: Customer/Agent are represented as one User entity
  - Ticket acts as aggregate and manages lifecycle of messages

### Level 3: Websockets + External broker + Outbox Pattern
- Once the domain model is more refined you split User in Customer/Agent entities
- Since in a Chat both client/receiver are sender and receiver of data, websockets can be a good choice
  - Lower latency
  - Better browser support (SSE not for IE), might be important for older customers
  - Binary data supported
- For all levels `< #3`, event updates are handled on the fly and can get lost if the service goes down before those events are delivered to the client
- Using an event broker as intermediate can guarantee reliable delivery, data changes can be propagated for instance through CDC/Outbox pattern.
- Frontend: Depending on how complex things get you certainly want to have the frontend decoupled from the backend, do client-side rendering and introduce an SPA (e.g. React/Angular)

### Level 4: Scaling considerations + WebRTC
- At some point (considering things like domain complexity, team structures, performance requirements) a microservice architecture might start to pay off
- For instance this could be: CustomerService, AgentService, TicketService, MessageService, CDCService, SSEService and common services like API-Gateway, Load Balancer and so forth
- Even though with Websockets you can stream binary data, for video/audio streaming it is not performant enough, you would use WebRTC (streaming directly between both parties)

## Roadmap for Level 2
- With a little bit more time and effort (borrowing ideas from Level 3/4) I think this can grow into a viable solution
- Needs a persistence layer for both the entities (e.g. Postgres) and the events (e.g. Kafka)
- Potentially there can be a huge amount of customers (~ 7,500 Companies/1.5 Million Employees). So to really consider this for production I would set up performance tests and see e.g. how many SSE connections my backend can manage
- Then I would think about scaling, which components should I extract out, how to horizontally scale those components?
- Regarding the existing codebase I would continue making it better, just to name a few things:
  - Put more time and effort into clean code & modeling the entities
      - Domain objects are 'misused' as DTO objects
      - Modelling domain events, which then can be just forwarded via SSE/Websocket
      - For example: `MessageCreated`, `TicketCreated` or `TicketResolved`. This would allow to update other parts of the UI in realtime.
  - Handling many event type with different DOM changes can get quite complex with htmx, so I would consider switching to client-side rendering e.g. an SPA (e.g. with Angular)
  - Authentication is currently session-based via form, probably the service would need to integrate into some OAuth flow
  - Add proper Exception Handling