// Hand-labelled synthetic development set, not independent validation or hiring data.
export const fixtures = [
 ['p01', 'Develop automated tests for web applications.', 'Created a regression suite that checks the checkout flow.', true],
 ['p02', 'Clean datasets before analysis.', 'Removed duplicate records and handled missing values in sales data.', true],
 ['p03', 'Optimize database query performance.', 'Added indexes and reduced the time taken by SQL queries.', true],
 ['p04', 'Collaborate with cross-functional teams.', 'Worked with designers and engineers to deliver a customer portal.', true],
 ['p05', 'Build REST APIs for backend services.', 'Implemented HTTP endpoints for creating and retrieving orders.', true],
 ['p06', 'Evaluate machine learning models.', 'Compared classification precision and recall using cross-validation.', true],
 ['p07', 'Conduct user research.', 'Interviewed users and tested prototypes to identify usability issues.', true],
 ['p08', 'Manage deployment pipelines.', 'Configured continuous integration and automated application releases.', true],
 ['n01', 'Develop automated tests for web applications.', 'Designed posters for a college cultural festival.', false],
 ['n02', 'Optimize database query performance.', 'Prepared a presentation about effective communication.', false],
 ['n03', 'Build REST APIs for backend services.', 'Analyzed crop growth using field measurements.', false],
 ['n04', 'Conduct user research.', 'Maintained Linux server backups and disk partitions.', false],
 ['h01', 'Develop automated tests.', 'I have no experience developing automated tests.', false],
 ['h02', 'Build REST APIs.', 'I plan to learn how to build REST APIs.', false],
 ['h03', 'Manage production deployments.', 'Read an introductory article about production deployments.', false],
 ['h04', 'Hold a masters degree in computer science.', 'Completed a short online introduction to computer science.', false],
].map(([id, requirement, evidence, expected]) => ({ id, requirement, evidence, expected }))
