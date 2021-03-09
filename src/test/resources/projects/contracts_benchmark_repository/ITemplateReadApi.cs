using ESMS.Assessments.BusinessModels;
using ESMS.Core.Data.Context;
using ESMS.Core.Data.Entities;
using ESMS.Libraries.Api;
using System;

namespace ESMS.Assessments.Api.Contracts
{
    public interface ITemplateReadApi
        : IReadApi<SMSDB, AssessmentTemplate, TemplateModel, Guid>
    {
        TemplateModel GetSingleBy(Guid ManagedObjectTypeId);
    }
}