## Summary of Changes
<!-- Provide a clear, concise summary of the problem solved and the implementation details. -->
- 

## Architectural & Technical Decisions
<!-- Explain WHY this approach was chosen over alternatives, noting trade-offs if applicable. -->
- 

## Verification & Testing Evidence
<!-- The Iron Law: Evidence before claims. Detail commands executed and test results. -->
- [ ] Backend tests passing: `cd backend && mvn clean test`
- [ ] Frontend tests passing: `cd frontend && npm test`
- [ ] Frontend production build verified: `cd frontend && npm run build`
- [ ] Linter & Typechecks clean: `vue-tsc --noEmit`

## Security & Performance Checklist
- [ ] SSRF protections verified (if network I/O or URL input is touched)
- [ ] No secrets, tokens, or credentials hardcoded
- [ ] Thread safety / Virtual Threads compliance (no synchronized pinning across I/O)
