using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

[assembly: CLSCompliant(true)]
namespace Bravo
{
    /// <summary>
    /// CA1040: Avoid empty interfaces
    /// </summary>
    public interface ICA1040 { }

    /// <summary>
    /// Expected Finidngs:
    ///     - CA1040: Avoid empty interfaces
    /// </summary>
    class DesignFindings
    {
    }
}
