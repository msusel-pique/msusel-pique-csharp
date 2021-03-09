using ESMS.Assessments.BusinessModels;
using ESMS.Core.Data.Context;
using ESMS.Core.Data.Entities;
using ESMS.Libraries.Api;
using System;

namespace ESMS.Assessments.Api.Contracts
{
    public interface ITemplateWriteApi
        : IWriteApi<SMSDB, AssessmentTemplate, TemplateModel, Guid>
    {
        void SafeInsert(TemplateModel model);
        void SafeUpdate(TemplateModel model);
    }
}